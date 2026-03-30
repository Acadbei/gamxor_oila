#!/bin/sh
set -eu

JDK_HOME="${HOME}/.local/jdks/temurin-17.0.18+8/Contents/Home"

if [ ! -x "${JDK_HOME}/bin/java" ]; then
  echo "Local JDK topilmadi: ${JDK_HOME}" >&2
  echo "Kerak bo'lsa, JDK papkasini qayta yaratib oling." >&2
  exit 1
fi

export JAVA_HOME="${JDK_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"
export GRADLE_OPTS="-Dorg.gradle.java.home=${JAVA_HOME} ${GRADLE_OPTS:-}"

exec sh "$(dirname "$0")/../gradlew" "$@"
