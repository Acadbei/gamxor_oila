package com.example.myapplication.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.component.initialsFromName
import com.example.myapplication.ui.theme.GlowMint
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky
import java.io.File

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onSignOut: () -> Unit,
    onSaveProfile: (CaregiverProfile) -> DemoActionResult,
    onSendInvitation: (String, String, String) -> DemoActionResult,
    onRegister: () -> Unit,
    onAction: (String) -> Unit
) {
    val context = LocalContext.current
    val profile = uiState.profile
    val splitName = rememberSaveable(profile.fullName) { splitFullName(profile.fullName) }

    var firstName by rememberSaveable(profile.fullName) { mutableStateOf(splitName.first) }
    var lastName by rememberSaveable(profile.fullName) { mutableStateOf(splitName.second) }
    var familyLabel by rememberSaveable(profile.familyLabel) { mutableStateOf(profile.familyLabel) }
    var phone by rememberSaveable(profile.phone) { mutableStateOf(normalizeUzPhone(profile.phone)) }
    var address by rememberSaveable(profile.address) { mutableStateOf(profile.address) }
    var avatarSeed by rememberSaveable(profile.avatarSeed) { mutableIntStateOf(profile.avatarSeed) }
    var avatarUri by rememberSaveable(profile.avatarUri) { mutableStateOf(profile.avatarUri) }
    var isEditingPersonal by rememberSaveable { mutableStateOf(false) }
    var showAvatarActions by rememberSaveable { mutableStateOf(false) }

    var inviteName by rememberSaveable { mutableStateOf("") }
    var inviteRelation by rememberSaveable { mutableStateOf("") }
    var invitePhone by rememberSaveable { mutableStateOf("+998 ") }

    fun resetDraft() {
        val resetName = splitFullName(profile.fullName)
        firstName = resetName.first
        lastName = resetName.second
        familyLabel = profile.familyLabel
        phone = normalizeUzPhone(profile.phone)
        address = profile.address
        avatarSeed = profile.avatarSeed
        avatarUri = profile.avatarUri
        isEditingPersonal = false
    }

    fun buildDraftProfile(): CaregiverProfile {
        return profile.copy(
            fullName = buildFullName(firstName, lastName),
            phone = phone,
            familyLabel = familyLabel,
            address = address,
            avatarSeed = avatarSeed,
            avatarUri = avatarUri
        )
    }

    fun persistAvatar(newUri: String) {
        avatarUri = newUri
        val result = onSaveProfile(
            if (isEditingPersonal) buildDraftProfile().copy(avatarUri = newUri)
            else profile.copy(avatarUri = newUri, avatarSeed = avatarSeed)
        )
        onAction(result.message)
    }

    fun copyAvatarAndPersist(sourceUri: Uri) {
        val storedAvatarUri = storeAvatarLocally(context, sourceUri)
        if (storedAvatarUri == null) {
            onAction("Profil rasmini saqlab bo'lmadi.")
            return
        }
        persistAvatar(storedAvatarUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            copyAvatarAndPersist(uri)
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            copyAvatarAndPersist(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.White,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
                    )
                )
            ),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileImageAvatar(
                                initials = initialsFromName(buildFullName(firstName, lastName)),
                                seed = avatarSeed,
                                avatarUri = avatarUri,
                                onClick = { showAvatarActions = true }
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = buildFullName(firstName, lastName).ifBlank { "Profil" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = familyLabel.ifBlank { uiState.familyLabel },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        SymbolChip(
                            icon = Icons.Default.VerifiedUser,
                            label = if (uiState.isRegistered) "Ro'yxatdan o'tgan" else "Ro'yxatdan o'tmagan",
                            accent = if (uiState.isRegistered) GlowMint else GlowSand
                        )
                    }

                    InfoLine(Icons.Default.Phone, "Telefon", phone)
                    InfoLine(Icons.Default.Home, "Manzil", address.ifBlank { "Manzil kiritilmagan" })

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.Group,
                            label = "${uiState.members.size} a'zo ulangan",
                            accent = GlowSky
                        )
                        SymbolChip(
                            icon = Icons.Default.LocationOn,
                            label = uiState.lastSyncLabel,
                            accent = GlowSand
                        )
                    }

                    Text(
                        text = "Profil rasmini boshqarish uchun rasm ustiga bosing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Person,
                title = "Shaxsiy ma'lumotlar",
                subtitle = "Ko'rish va tahrirlash"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditingPersonal) "Tahrirlash rejimi" else "Shaxsiy ma'lumotlar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        TextButton(
                            onClick = {
                                if (isEditingPersonal) {
                                    resetDraft()
                                } else {
                                    isEditingPersonal = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isEditingPersonal) Icons.Default.DeleteOutline else Icons.Default.Edit,
                                contentDescription = null
                            )
                            Box(modifier = Modifier.width(8.dp))
                            Text(if (isEditingPersonal) "Bekor qilish" else "Tahrirlash")
                        }
                    }

                    if (isEditingPersonal) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Ism") }
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Familiya") }
                            )
                        }

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = normalizeUzPhone(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            label = { Text("Telefon raqami") }
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Yashash manzili") }
                        )

                        OutlinedTextField(
                            value = familyLabel,
                            onValueChange = { familyLabel = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            label = { Text("Oila nomi") }
                        )

                        Button(
                            onClick = {
                                val result = onSaveProfile(buildDraftProfile())
                                onAction(result.message)
                                if (result.success) {
                                    isEditingPersonal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Box(modifier = Modifier.width(8.dp))
                            Text("Saqlash")
                        }
                    } else {
                        InfoLine(Icons.Default.Person, "Ism familiya", buildFullName(firstName, lastName))
                        InfoLine(Icons.Default.Phone, "Telefon", phone)
                        InfoLine(Icons.Default.Home, "Manzil", address.ifBlank { "Kiritilmagan" })
                        InfoLine(Icons.Default.Group, "Oila nomi", familyLabel.ifBlank { "Kiritilmagan" })
                    }
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Add,
                title = "Oila a'zosini qo'shish",
                subtitle = "Telefon raqami orqali taklif yuborish"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = inviteName,
                        onValueChange = { inviteName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Ismi") }
                    )

                    OutlinedTextField(
                        value = inviteRelation,
                        onValueChange = { inviteRelation = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text("Qarindoshligi") }
                    )

                    OutlinedTextField(
                        value = invitePhone,
                        onValueChange = { invitePhone = normalizeUzPhone(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        label = { Text("Telefon") }
                    )

                    Button(
                        onClick = {
                            val result = onSendInvitation(inviteName, inviteRelation, invitePhone)
                            onAction(result.message)
                            if (result.success) {
                                inviteName = ""
                                inviteRelation = ""
                                invitePhone = "+998 "
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Box(modifier = Modifier.width(8.dp))
                        Text("Oila a'zosini qo'shish")
                    }
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.VerifiedUser,
                title = "Amallar",
                subtitle = "Ro'yxatdan o'tish va chiqish"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!uiState.isRegistered) {
                        Button(
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null)
                            Box(modifier = Modifier.width(8.dp))
                            Text("Ro'yxatdan o'tish")
                        }
                    }

                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Box(modifier = Modifier.width(8.dp))
                        Text("Hisobdan chiqish")
                    }
                }
            }
        }
    }

    if (showAvatarActions) {
        AlertDialog(
            onDismissRequest = { showAvatarActions = false },
            title = { Text("Profil rasmi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showAvatarActions = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Galereyadan tanlash")
                    }
                    TextButton(
                        onClick = {
                            showAvatarActions = false
                            fileLauncher.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fayldan tanlash")
                    }
                    if (avatarUri.isNotBlank()) {
                        TextButton(
                            onClick = {
                                showAvatarActions = false
                                deleteStoredAvatar(context, avatarUri)
                                persistAvatar("")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rasmni o'chirish")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarActions = false }) {
                    Text("Yopish")
                }
            }
        )
    }
}

@Composable
private fun ProfileImageAvatar(
    initials: String,
    seed: Int,
    avatarUri: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val avatarBitmap = remember(avatarUri) {
        avatarUri.takeIf { it.isNotBlank() }?.let { uri ->
            loadAvatarBitmap(context, uri)
        }
    }

    Box(
        modifier = Modifier.size(78.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop
            )
        } else {
            InitialsAvatar(
                initials = initials,
                seed = seed,
                modifier = Modifier.clickable(onClick = onClick),
                size = 74.dp
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun loadAvatarBitmap(context: android.content.Context, avatarUri: String): Bitmap? {
    return runCatching {
        val uri = Uri.parse(avatarUri)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }.getOrNull()
}

private fun storeAvatarLocally(
    context: android.content.Context,
    sourceUri: Uri
): String? {
    return runCatching {
        val avatarFile = File(context.filesDir, "profile-avatar.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            avatarFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return null
        Uri.fromFile(avatarFile).toString()
    }.getOrNull()
}

private fun deleteStoredAvatar(
    context: android.content.Context,
    avatarUri: String
) {
    val fileUri = runCatching { Uri.parse(avatarUri) }.getOrNull() ?: return
    if (fileUri.scheme != "file") return

    val path = fileUri.path ?: return
    if (!path.startsWith(context.filesDir.absolutePath)) return

    runCatching {
        File(path).takeIf { it.exists() }?.delete()
    }
}

private fun splitFullName(fullName: String): Pair<String, String> {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "" to ""
        parts.size == 1 -> parts.first() to ""
        else -> parts.first() to parts.drop(1).joinToString(" ")
    }
}

private fun buildFullName(firstName: String, lastName: String): String =
    listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ")
