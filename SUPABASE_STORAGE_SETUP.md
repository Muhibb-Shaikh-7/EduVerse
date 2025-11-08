# Supabase Storage Setup Guide for EduVerse

## Overview

This guide walks you through setting up Supabase Storage for EduVerse as a replacement for Firebase
Storage, while keeping Firebase Auth, Firestore, and ML Kit OCR.

**Architecture:**

- ✅ **Firebase Authentication** - User auth and management
- ✅ **Firebase Firestore** - Database for app data
- ✅ **Supabase Storage** - File storage (PDFs, images)
- ✅ **ML Kit OCR** - Text recognition

---

## Table of Contents

1. [Why Supabase Storage?](#why-supabase-storage)
2. [Supabase Project Setup](#supabase-project-setup)
3. [Configure Supabase in Android App](#configure-supabase-in-android-app)
4. [Create Storage Buckets](#create-storage-buckets)
5. [Set Up Security Policies](#set-up-security-policies)
6. [Testing Guide](#testing-guide)
7. [Migration from Firebase Storage](#migration-from-firebase-storage)

---

## Why Supabase Storage?

### Advantages:

1. **Cost-Effective**: More generous free tier (1GB storage, 2GB bandwidth)
2. **Better Performance**: Built on AWS S3 with CDN
3. **Flexible Policies**: Row-level security policies
4. **Open Source**: Self-hostable if needed
5. **Modern API**: RESTful and intuitive
6. **Better Upload Experience**: Resumable uploads, progress tracking

### Comparison:

| Feature | Firebase Storage | Supabase Storage |
|---------|-----------------|-------------------|
| Free Tier | 5GB storage, 1GB/day download | 1GB storage, 2GB bandwidth |
| Max File Size | 5TB | 5GB (configurable) |
| CDN | Yes | Yes (via CloudFlare) |
| Resumable Uploads | Yes | Yes |
| Fine-grained Access | Security Rules | Row-level policies |

---

## Supabase Project Setup

### Step 1: Create Supabase Account

1. Go to [https://supabase.com](https://supabase.com)
2. Click **"Start your project"**
3. Sign up with GitHub, Google, or Email

### Step 2: Create New Project

1. Click **"New Project"**
2. Choose your organization (or create one)
3. Fill in project details:
    - **Name**: `eduverse` or `eduverse-storage`
    - **Database Password**: Generate a strong password (save it securely!)
    - **Region**: Choose closest to your users (e.g., `US East`, `EU Central`)
    - **Pricing Plan**: Start with **Free** tier
4. Click **"Create new project"**
5. Wait 2-3 minutes for project initialization

### Step 3: Get API Credentials

1. In your Supabase dashboard, go to **Settings** → **API**
2. Copy the following credentials:
    - **Project URL**: `https://xxxxx.supabase.co`
    - **anon/public key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Keep these safe - you'll need them for the Android app

---

## Configure Supabase in Android App

### Step 1: Update Firebase Module

Open `app/src/main/java/com/example/eduverse/di/FirebaseModule.kt` and replace the Supabase
credentials:

```kotlin
@Provides
@Singleton
fun provideSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = "https://YOUR_PROJECT_ID.supabase.co", // Replace with your URL
        supabaseKey = "YOUR_ANON_KEY" // Replace with your anon key
    ) {
        install(Storage) {
            // Storage configuration
        }
    }
}
```

**Example:**

```kotlin
supabaseUrl = "https://abcdefghijklmnop.supabase.co"
supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprbG1ub3AiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYyOTQxMjAwMCwiZXhwIjoxOTQ0OTg4MDAwfQ.example_signature"
```

### Step 2: Sync Gradle

1. Click **"Sync Now"** in Android Studio
2. Wait for dependencies to download
3. Rebuild the project

---

## Create Storage Buckets

### Step 1: Access Storage Dashboard

1. In Supabase dashboard, click **Storage** in the left sidebar
2. You'll see the storage interface

### Step 2: Create Required Buckets

Create 4 buckets for different file types:

#### 1. Materials Bucket

1. Click **"New bucket"**
2. Name: `materials`
3. Public bucket: **Yes** (for easy access)
4. File size limit: `10 MB`
5. Allowed MIME types: `application/pdf, image/jpeg, image/png`
6. Click **"Create bucket"**

#### 2. Profiles Bucket

1. Click **"New bucket"**
2. Name: `profiles`
3. Public bucket: **Yes**
4. File size limit: `5 MB`
5. Allowed MIME types: `image/jpeg, image/png`
6. Click **"Create bucket"**

#### 3. Flashcards Bucket

1. Click **"New bucket"**
2. Name: `flashcards`
3. Public bucket: **Yes**
4. File size limit: `5 MB`
5. Allowed MIME types: `image/jpeg, image/png`
6. Click **"Create bucket"**

#### 4. Quizzes Bucket

1. Click **"New bucket"**
2. Name: `quizzes`
3. Public bucket: **Yes**
4. File size limit: `10 MB`
5. Allowed MIME types: `application/pdf, image/jpeg, image/png`
6. Click **"Create bucket"**

---

## Set Up Security Policies

### Understanding Supabase Policies

Supabase uses PostgreSQL Row Level Security (RLS) for fine-grained access control.

### Step 1: Enable RLS on Buckets

For each bucket, we need to set up policies:

#### Materials Bucket Policies

1. Go to **Storage** → `materials` bucket
2. Click **Policies** tab
3. Click **"New Policy"**

**Policy 1: Allow Authenticated Users to Upload**

```sql
CREATE POLICY "Allow authenticated uploads"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'materials');
```

**Policy 2: Allow Public Read**

```sql
CREATE POLICY "Allow public read"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'materials');
```

**Policy 3: Allow Users to Delete Their Own Files**

```sql
CREATE POLICY "Allow users to delete own files"
ON storage.objects FOR DELETE
TO authenticated
USING (
  bucket_id = 'materials' 
  AND (storage.foldername(name))[1] = auth.uid()::text
);
```

**Policy 4: Allow Users to Update Their Own Files**

```sql
CREATE POLICY "Allow users to update own files"
ON storage.objects FOR UPDATE
TO authenticated
USING (
  bucket_id = 'materials' 
  AND (storage.foldername(name))[1] = auth.uid()::text
);
```

#### Profiles Bucket Policies

**Policy 1: Allow Users to Upload Their Profile Picture**

```sql
CREATE POLICY "Users can upload profile picture"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
  bucket_id = 'profiles' 
  AND (storage.foldername(name))[1] = auth.uid()::text
);
```

**Policy 2: Allow Public Read**

```sql
CREATE POLICY "Public profile pictures"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'profiles');
```

**Policy 3: Allow Users to Update Their Profile**

```sql
CREATE POLICY "Users can update profile"
ON storage.objects FOR UPDATE
TO authenticated
USING (
  bucket_id = 'profiles' 
  AND (storage.foldername(name))[1] = auth.uid()::text
);
```

### Step 2: Apply Policies via Dashboard

1. Click **"New Policy"** for each bucket
2. Choose **"Create a policy from scratch"**
3. Paste the SQL policy code
4. Click **"Review"** then **"Save policy"**

### Alternative: Use Policy Templates

Supabase provides templates:

1. Click **"New Policy"**
2. Choose from templates:
    - **"Allow public read access"**
    - **"Allow authenticated users to upload"**
    - **"Allow users to CRUD their own data"**
3. Customize as needed

---

## Testing Guide

### Prerequisites

1. ✅ Supabase project created
2. ✅ API credentials added to `FirebaseModule.kt`
3. ✅ All 4 buckets created
4. ✅ Security policies applied
5. ✅ Gradle synced successfully

### Test 1: Upload PDF (Teacher)

#### In the App

1. Open the app
2. Register/Login as Teacher
3. Go to **"Upload Material"**
4. Select a PDF file
5. Fill in details and upload
6. ✅ **Expected**: Success message displayed

#### Verify in Supabase Dashboard

1. Go to **Storage** → `materials` bucket
2. Navigate to `{teacherId}/` folder
3. ✅ **Expected**: See the uploaded PDF
4. Click on file → **"Get URL"**
5. ✅ **Expected**: URL should work in browser

### Test 2: Upload Image (Teacher)

#### In the App

1. Stay logged in as Teacher
2. Go to **"OCR Processing"**
3. Select an image with text
4. Click **"Upload & Extract"**
5. ✅ **Expected**: Image uploaded, text extracted

#### Verify in Supabase

1. Go to **Storage** → `materials` bucket
2. Check `{teacherId}/` folder
3. ✅ **Expected**: See uploaded image

### Test 3: Download File (Student)

#### In the App

1. Logout and register as Student
2. Browse materials
3. Click on a material
4. Click **"Download"** or **"View"**
5. ✅ **Expected**: File loads/downloads successfully

### Test 4: Upload Profile Picture

#### In the App

1. Go to Profile/Settings
2. Click **"Change Profile Picture"**
3. Select an image
4. Click **"Upload"**
5. ✅ **Expected**: Profile picture updates

#### Verify in Supabase

1. Go to **Storage** → `profiles` bucket
2. Check `{userId}/profile.jpg`
3. ✅ **Expected**: See profile image

### Test 5: Delete File (Teacher)

#### In the App

1. Login as Teacher
2. Go to uploaded materials
3. Select a material
4. Click **"Delete"**
5. ✅ **Expected**: Material deleted

#### Verify in Supabase

1. Go to **Storage** → `materials` bucket
2. Check teacher's folder
3. ✅ **Expected**: File is removed

### Test 6: Access Control

#### Test Unauthorized Access

1. Try to delete another user's file
2. ✅ **Expected**: Permission denied error

#### Test Public Access

1. Copy a public file URL
2. Open in incognito/private browser
3. ✅ **Expected**: File accessible (for public buckets)

---

## Migration from Firebase Storage

If you had Firebase Storage before:

### Step 1: Export Existing Files

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Download files
gsutil -m cp -r gs://your-firebase-bucket.appspot.com ./firebase-backup
```

### Step 2: Upload to Supabase

Use the Supabase CLI or dashboard to bulk upload:

```bash
# Install Supabase CLI
npm install -g supabase

# Login
supabase login

# Link project
supabase link --project-ref your-project-ref

# Upload files
supabase storage cp ./firebase-backup/* supabase://materials/
```

### Step 3: Update Database URLs

Run a migration script to update Firestore `fileUrl` fields:

```kotlin
suspend fun migrateStorageUrls() {
    val materials = firestore.collection("materials").get().await()
    
    materials.documents.forEach { doc ->
        val oldUrl = doc.getString("fileUrl")
        if (oldUrl?.contains("firebase") == true) {
            // Convert to Supabase URL
            val newUrl = convertToSupabaseUrl(oldUrl)
            
            doc.reference.update("fileUrl", newUrl).await()
        }
    }
}
```

---

## Implementation Details

### Upload Implementation

```kotlin
// Upload PDF
val result = supabaseStorageService.uploadPDF(
    uri = fileUri,
    userId = currentUserId,
    fileName = "calculus_notes.pdf"
)

result.onSuccess { publicUrl ->
    println("File uploaded: $publicUrl")
    // Save URL to Firestore
}
```

### Download Implementation

```kotlin
// Get public URL
val url = supabaseStorageService.getPublicUrl(
    bucket = "materials",
    storagePath = "userId/file.pdf"
)

// Load in app (e.g., with Coil for images)
AsyncImage(
    model = url,
    contentDescription = "Material"
)
```

### Delete Implementation

```kotlin
// Delete file
val result = supabaseStorageService.deleteFile(
    bucket = "materials",
    storagePath = "userId/file.pdf"
)

result.onSuccess {
    println("File deleted successfully")
}
```

---

## Monitoring and Debugging

### Check Upload Status

In Supabase Dashboard:

1. Go to **Storage**
2. Click on a bucket
3. View all files and their metadata
4. Check file sizes and upload times

### View Storage Usage

1. Go to **Settings** → **Usage**
2. Monitor:
    - **Storage**: Total GB used
    - **Bandwidth**: GB transferred
    - **API Requests**: Number of storage operations

### Debug Issues

#### Issue: "Bucket not found"

**Solution**: Verify bucket name matches exactly (case-sensitive)

#### Issue: "Permission denied"

**Solution**: Check RLS policies are correctly applied

#### Issue: "File too large"

**Solution**: Increase bucket file size limit in settings

### Enable Logs

```kotlin
// Add logging to SupabaseStorageService
suspend fun uploadPDF(...): Result<String> {
    return try {
        Log.d("Supabase", "Starting upload: $fileName")
        // ... upload logic
        Log.d("Supabase", "Upload successful: $publicUrl")
        Result.success(publicUrl)
    } catch (e: Exception) {
        Log.e("Supabase", "Upload failed", e)
        Result.failure(e)
    }
}
```

---

## Performance Optimization

### 1. Image Compression

```kotlin
// Compress before upload
fun compressImage(uri: Uri): ByteArray {
    val bitmap = BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(uri)
    )
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return stream.toByteArray()
}
```

### 2. Progress Tracking

```kotlin
// Show upload progress
storage.from(bucket).upload(path, data) {
    contentType = "application/pdf"
    onUploadProgress = { progress ->
        val percentage = (progress.toFloat() / data.size) * 100
        updateProgressBar(percentage)
    }
}
```

### 3. Caching

Use Coil for automatic image caching:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(supabaseImageUrl)
        .crossfade(true)
        .memoryCacheKey(fileName)
        .diskCacheKey(fileName)
        .build(),
    contentDescription = null
)
```

---

## Security Best Practices

1. **Never expose service_role key** in Android app
2. **Use anon key** for client operations
3. **Implement RLS policies** for all buckets
4. **Validate file types** before upload
5. **Set file size limits** on buckets
6. **Use HTTPS** for all URLs
7. **Implement rate limiting** if needed

---

## Cost Estimation

### Free Tier Limits

- **Storage**: 1 GB
- **Bandwidth**: 2 GB/month
- **API Requests**: Unlimited on free tier

### When to Upgrade

Upgrade to Pro ($25/month) when you exceed:

- 8 GB storage
- 50 GB bandwidth
- Need custom domains
- Need advanced security features

### Cost Comparison

For 100 GB storage + 100 GB bandwidth/month:

- **Supabase Pro**: $25/month (included)
- **Firebase Blaze**: ~$2.60 (storage) + ~$12 (bandwidth) = ~$14.60/month

---

## Next Steps

1. ✅ Create Supabase project
2. ✅ Add credentials to Android app
3. ✅ Create storage buckets
4. ✅ Set up security policies
5. ✅ Test file upload/download
6. ✅ Test access control
7. 🔄 Monitor usage and performance
8. 🔄 Set up backup strategy
9. 🔄 Configure CDN if needed
10. 🔄 Implement file versioning (optional)

---

## Support & Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Storage Documentation](https://supabase.com/docs/guides/storage)
- [Security Policies Guide](https://supabase.com/docs/guides/storage/security/access-control)
- [Supabase Discord](https://discord.supabase.com/)
- [GitHub Issues](https://github.com/supabase/supabase/issues)

---

## Conclusion

Your EduVerse app now uses:

- ✅ **Firebase Auth** for authentication
- ✅ **Firebase Firestore** for database
- ✅ **Supabase Storage** for file storage
- ✅ **ML Kit OCR** for text recognition

This hybrid approach gives you the best of both platforms!

**Benefits:**

- 🚀 Better storage performance
- 💰 More cost-effective
- 🔒 Flexible security policies
- 🌍 Global CDN
- 📊 Better analytics

Happy coding! 🎉
