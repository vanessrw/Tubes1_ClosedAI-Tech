# Tugas Besar 1 IF2211 Strategi Algoritma
Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “Galaxio”

## Algoritma Greedy yang Diimplementasikan

Element algoritma greedy:

1. Himpunan kandidat: Semua fungsi yang tersedia
   Semua fungsi merupakan kandidat dari algoritma untuk menentukan aksi dari bot.
  
2. Himpunan solusi: Fungsi paling optimal dari aspek makan, penyerangan, dan pertahanan
  
3. Fungsi solusi: Fungsi untuk memeriksa apakah aksi yang dipilih sudah menghasilkan hasil optimal
  
4. Fungsi seleksi: Memeriksa apakah aksi terpilih merupakan aksi yang aman dan tidak merugikan
  
5. Fungsi kelayakan: Memeriksa apakah aksi yang dilakukan valid
  
6. Fungsi objektif: Memeriksa bahwa aksi yang dilakukan akan menghasilkan pertambahan ukuran paling optimal

Algoritma greedy yang paling mangkus saat digunakan untuk memenuhi objektif permainan adalah algoritma greedy berdasarkan densitas, yaitu pembagian antara ukuran musuh dengan jarak musuh. Bot akan menyerang musuh dengan densitas tertinggi, yang berarti merupakan bot lebih kecil yang terbesar dan memiliki posisi yang paling dekat dengan bot. Semakin besar densitasnya, maka semakin optimal pula hasil penyerangannya.
  
## Requirement Program dan Set-up
1.	Java (minimal Java 11): https://www.oracle.com/java/technologies/downloads/#java
2.	IntelIiJ IDEA         : https://www.jetbrains.com/idea/
3.	NodeJS                : https://nodejs.org/en/download/
4.	.Net Core 3.1         : https://dotnet.microsoft.com/en-us/download/dotnet/3.1

## Cara Penggunaan
1. Open project ini menggunakan code editor (disarankan menggunakan IntelliJ IDEA)
2. Install project ini dengan plugin Maven yang sudah tersedia pada IntellIJ
3. Clone `starter-pack` dari repository Entellect Challenge 2021:
   https://github.com/EntelectChallenge/2021-Galaxio
4. lakukan `mvn clean package` pada terminal hingga terbentuk executable jar file   
5. Pindahkan file `.jar` pada folder `target` di repository ini ke folder `starter-pack` yang telah diclone
6. Jalankan command `./run.bat` pada terminal
7. Jalankan aplikasi Visualizer Galaxio pada `starter-pack`
8. Buka menu `Options` lalu salin path folder “logger-publish” pada “Log Files Lovation”. Kemudian lakukan `Save`
9. Pilih menu `Load`
10. Pilih file JSON yang ingin diload pada “Game Log”, lalu “Start”
11. Pilih menu `Start`
12. Selamat, permainan sudah siap untuk dijalankan !

## Sumber Game Engine dan Rules
1. Game engine: https://github.com/EntelectChallenge/2021-Galaxio
2. Game rules: https://github.com/EntelectChallenge/2021-Galaxio/blob/develop/game-engine/game-rules.md

## Anggota Kelompok
#### [Enrique Alifio Ditya / 13521142](https://github.com/AlifioDitya)
#### [Rava Maulana Azzikri / 13521149](https://github.com/RMA1403)
#### [Vanessa Rebecca Wiyono / 13521151](https://github.com/vanessrw)
  
