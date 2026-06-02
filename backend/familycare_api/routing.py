from __future__ import annotations

import json
import math
from dataclasses import dataclass
from functools import lru_cache
from itertools import pairwise
from urllib.parse import quote
from urllib import error, request

import networkx as nx


OVERPASS_URLS = (
    "https://overpass-api.de/api/interpreter",
    "https://lz4.overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
)
OVERPASS_TIMEOUT_SECONDS = 40


class RoutePlanningError(RuntimeError):
    pass


@dataclass(frozen=True)
class PlannedRoute:
    alert_id: int
    member_id: int
    member_name: str
    relation: str
    phone: str
    source_member_id: int
    source_lat: float
    source_lon: float
    target_lat: float
    target_lon: float
    dijkstra_points: list[dict]
    astar_points: list[dict]
    dijkstra_length_meters: float
    astar_length_meters: float
    is_same_path: bool
    graph_nodes: int
    graph_edges: int


def haversine_meters(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    radius_m = 6_371_000.0
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = (
        math.sin(delta_phi / 2) ** 2
        + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2
    )
    return 2 * radius_m * math.atan2(math.sqrt(a), math.sqrt(1 - a))


def plan_route_for_points(
    start_lat: float,
    start_lon: float,
    target_lat: float,
    target_lon: float,
) -> tuple[nx.DiGraph, list[int], list[int], float, float]:
    last_error: Exception | None = None

    for padding in (0.01, 0.02, 0.04, 0.06):
        try:
            graph = _load_route_graph(start_lat, start_lon, target_lat, target_lon, padding)
        except RoutePlanningError as exc:
            last_error = exc
            continue

        source_node = _nearest_node(graph, start_lat, start_lon)
        target_node = _nearest_node(graph, target_lat, target_lon)

        if source_node == target_node:
            node_path = [source_node]
            return graph, node_path, node_path, 0.0, 0.0

        try:
            dijkstra_nodes = nx.dijkstra_path(graph, source_node, target_node, weight="length")
            astar_nodes = nx.astar_path(
                graph,
                source_node,
                target_node,
                heuristic=lambda a, b: haversine_meters(
                    graph.nodes[a]["lat"],
                    graph.nodes[a]["lon"],
                    graph.nodes[b]["lat"],
                    graph.nodes[b]["lon"],
                ),
                weight="length",
            )
            return (
                graph,
                dijkstra_nodes,
                astar_nodes,
                _path_length(graph, dijkstra_nodes),
                _path_length(graph, astar_nodes),
            )
        except (nx.NetworkXNoPath, nx.NodeNotFound) as exc:
            last_error = exc
            continue

    raise RoutePlanningError("Siz va SOS a'zosi orasida mos yo'l topilmadi.") from last_error


def route_points(graph: nx.DiGraph, node_path: list[int]) -> list[dict]:
    return [
        {
            "lat": graph.nodes[node_id]["lat"],
            "lon": graph.nodes[node_id]["lon"],
        }
        for node_id in node_path
    ]


def plan_route_for_alert(
    *,
    alert_id: int,
    source_member_id: int,
    source_lat: float,
    source_lon: float,
    target_member_id: int,
    target_name: str,
    relation: str,
    phone: str,
    target_lat: float,
    target_lon: float,
) -> PlannedRoute:
    graph, dijkstra_nodes, astar_nodes, dijkstra_length, astar_length = plan_route_for_points(
        source_lat,
        source_lon,
        target_lat,
        target_lon,
    )

    return PlannedRoute(
        alert_id=alert_id,
        member_id=target_member_id,
        member_name=target_name,
        relation=relation,
        phone=phone,
        source_member_id=source_member_id,
        source_lat=source_lat,
        source_lon=source_lon,
        target_lat=target_lat,
        target_lon=target_lon,
        dijkstra_points=route_points(graph, dijkstra_nodes),
        astar_points=route_points(graph, astar_nodes),
        dijkstra_length_meters=dijkstra_length,
        astar_length_meters=astar_length,
        is_same_path=dijkstra_nodes == astar_nodes,
        graph_nodes=graph.number_of_nodes(),
        graph_edges=graph.number_of_edges(),
    )


def _bbox_from_points(
    start_lat: float,
    start_lon: float,
    target_lat: float,
    target_lon: float,
    padding_degrees: float,
) -> tuple[float, float, float, float]:
    south = min(start_lat, target_lat) - padding_degrees
    west = min(start_lon, target_lon) - padding_degrees
    north = max(start_lat, target_lat) + padding_degrees
    east = max(start_lon, target_lon) + padding_degrees
    return south, west, north, east


def _load_route_graph(
    start_lat: float,
    start_lon: float,
    target_lat: float,
    target_lon: float,
    padding_degrees: float,
) -> nx.DiGraph:
    bbox = _bbox_from_points(start_lat, start_lon, target_lat, target_lon, padding_degrees)
    graph = _cached_graph_for_bbox(*bbox)
    if graph.number_of_nodes() < 2 or graph.number_of_edges() < 1:
        raise RoutePlanningError("Marshrut uchun yo'l ma'lumoti topilmadi.")
    return graph


@lru_cache(maxsize=16)
def _cached_graph_for_bbox(
    south: float,
    west: float,
    north: float,
    east: float,
) -> nx.DiGraph:
    query = (
        '[out:json][timeout:40];'
        '('
        f'way["highway"]({south},{west},{north},{east});'
        ');'
        'out geom;'
    )
    last_error: Exception | None = None
    for overpass_url in OVERPASS_URLS:
        try:
            with request.urlopen(
                request.Request(
                    f"{overpass_url}?data={quote(query)}",
                    headers={"User-Agent": "FamilyCare/1.0", "Accept": "application/json"},
                ),
                timeout=OVERPASS_TIMEOUT_SECONDS,
            ) as response:
                payload = json.loads(response.read().decode("utf-8"))
                break
        except (error.URLError, error.HTTPError, TimeoutError, json.JSONDecodeError) as exc:
            last_error = exc
            payload = None
            continue
    else:
        raise RoutePlanningError("OSM xarita ma'lumotini yuklab bo'lmadi.") from last_error

    elements = (payload or {}).get("elements", [])
    if not elements:
        raise RoutePlanningError("Bu hudud uchun yo'l ma'lumoti topilmadi.")

    return _build_graph(elements)


def _build_graph(elements: list[dict]) -> nx.DiGraph:
    graph = nx.DiGraph()

    for way in elements:
        tags = way.get("tags") or {}
        if "highway" not in tags:
            continue

        way_id = int(way["id"])
        geometry = way.get("geometry") or []
        if len(geometry) < 2:
            continue

        for start_point, end_point in pairwise(geometry):
            start_node = _coord_node_id(start_point["lat"], start_point["lon"])
            end_node = _coord_node_id(end_point["lat"], end_point["lon"])
            start_lat, start_lon = float(start_point["lat"]), float(start_point["lon"])
            end_lat, end_lon = float(end_point["lat"]), float(end_point["lon"])
            length = haversine_meters(start_lat, start_lon, end_lat, end_lon)
            attributes = {
                "length": length,
                "way_id": way_id,
                "name": tags.get("name", ""),
                "highway": tags.get("highway", ""),
            }
            if start_node not in graph:
                graph.add_node(start_node, lat=start_lat, lon=start_lon)
            if end_node not in graph:
                graph.add_node(end_node, lat=end_lat, lon=end_lon)
            _add_edge(graph, start_node, end_node, attributes)
            _add_edge(graph, end_node, start_node, attributes)

    return graph


def _coord_node_id(lat: float, lon: float) -> tuple[float, float]:
    return (round(float(lat), 6), round(float(lon), 6))


def _add_edge(graph: nx.DiGraph, start_node: int, end_node: int, attributes: dict) -> None:
    existing = graph.get_edge_data(start_node, end_node)
    if existing is None or attributes["length"] < existing["length"]:
        graph.add_edge(start_node, end_node, **attributes)


def _nearest_node(graph: nx.DiGraph, lat: float, lon: float) -> int:
    if graph.number_of_nodes() == 0:
        raise RoutePlanningError("Yo'l grafi bo'sh.")

    return min(
        graph.nodes,
        key=lambda node_id: haversine_meters(
            lat,
            lon,
            graph.nodes[node_id]["lat"],
            graph.nodes[node_id]["lon"],
        ),
    )


def _path_length(graph: nx.DiGraph, node_path: list[int]) -> float:
    total = 0.0
    for start_node, end_node in pairwise(node_path):
        total += float(graph[start_node][end_node]["length"])
    return total
