# NFC Absensi - Aplikasi Absensi Mahasiswa

Aplikasi Android untuk mendata kehadiran seminar mahasiswa menggunakan NFC (Kartu Tanda Mahasiswa / KTM).

## Fitur Utama
1. **Manajemen Mahasiswa (Pre-register)**
   - Scan kartu KTM untuk mendaftarkan mahasiswa (UID, NIM, Nama, dll).
2. **Manajemen Seminar**
   - Tambah data seminar (Judul, Dosen, Harga, dll).
3. **Absensi Otomatis dengan NFC**
   - Scan kartu saat seminar berlangsung.
   - Cek validitas mahasiswa.
   - Input data tambahan (Kode Tagihan, Offline/Online).
4. **Export Excel**
   - Rekap absensi per seminar ke file `.xlsx`.

## Persyaratan Sistem
- Android Studio Iguana / Jellyfish (Recommended).
- Minimal SDK: 24 (Android 7.0).
- Target SDK: 34 (Android 14).
- Perangkat Android dengan fitur **NFC** untuk testing langsung.

## Cara Menggunakan (Android Studio)
1. Clone atau download source code ini.
2. Buka Android Studio -> `File` -> `Open` -> Pilih folder `NFCAbsensi`.
3. Tunggu proses Gradle Sync selesai.
4. Hubungkan HP Android (NFC enable) atau gunakan Emulator (tanpa fitur NFC).
5. Klik tombol `Run` (Segitiga Hijau).

## Simulasi Tanpa NFC
Jika Anda menggunakan Emulator atau HP tanpa NFC, aplikasi menyediakan tombol **Simulasi** untuk testing logika:
- **Menu Tambah Mahasiswa**: Klik "Simulasi Scan NFC" untuk generate UID acak.
- **Menu Detail Seminar**: Tekan dan Tahan (Long Press) tombol "Scan Absen (NFC)" untuk memunculkan dialog input UID manual.

## Catatan Build
- Jika terjadi error `MultiDex`, pastikan `multiDexEnabled = true` di `build.gradle`.
- Library Excel menggunakan Apache POI. Jika file size terlalu besar, Proguard akan menguranginya saat release build.

## Struktur Project
- `data/entity`: Model Room Database (Student, Event, StudentEvent).
- `data/dao`: Akses data database.
- `ui`: Fragment dan Activity.
- `utils`: Helper untuk NFC dan Excel Export.
