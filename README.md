# VoxAble — Erişilebilir Sesli Asistan

[![Android CI](https://github.com/vedatguldu/VoxAble-VA/actions/workflows/android-ci.yml/badge.svg)](https://github.com/vedatguldu/VoxAble-VA/actions/workflows/android-ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://developer.android.com/about/versions/lollipop)

**TalkBack uyumlu**, görme engelli kullanıcılar için tasarlanmış çok modüllü Android uygulaması.

<p align="center">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Hilt-DI-FF6F00?logo=dagger&logoColor=white" />
</p>

---

## 🏗️ Mimari

| Katman | Pattern |
|--------|--------|
| Sunum | MVVM (Jetpack Compose + ViewModel) |
| Domain | Use Case + Repository Interface |
| Veri | Repository Impl + Room + Retrofit + Firebase |
| DI | Hilt (Dagger) |
| Navigasyon | Jetpack Navigation Compose |

## 📦 Modül Yapısı

```
VoxAble/
├── app/                    → Ana modül: Activity, NavHost, Hilt Application
├── core/                   → BaseViewModel, BaseRepository, Resource, NetworkMonitor
├── core-ui/                → Compose tema, renk, tipografi, ortak bileşenler
├── core-accessibility/     → TalkBack yardımcıları, erişilebilirlik semantikleri
├── core-network/           → Retrofit + OkHttp + AuthInterceptor
├── core-database/          → Room veritabanı, DAO’lar, Entity’ler, DataStore
├── feature-auth/           → Giriş / Kayıt (Firebase Auth + Firestore)
├── feature-home/           → Ana sayfa, özellik kartları
├── feature-reader/         → Metin okuyucu (Android TTS, Türkçe)
├── feature-media/          → Medya oynatıcı (Media3 ExoPlayer)
├── feature-ocr/            → Metin tanıma (ML Kit + CameraX)
├── feature-currency/       → Döviz çevirici (API + önbellek)
├── feature-converter/      → Birim çevirici (uzunluk, ağırlık, sıcaklık, alan, hacim, hız)
├── feature-downloader/     → Dosya indirici (OkHttp + Room progress)
└── feature-settings/       → Ayarlar (DataStore, tema, erişilebilirlik, dil, çıkış)
```

## 🔧 Teknoloji Yığını

| Kategori | Kütüphane |
|----------|----------|
| Dil | Kotlin 2.0 |
| Min SDK | 21 (Android 5.0) |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (Dagger) 2.51 |
| Navigasyon | Navigation Compose 2.7 |
| Veritabanı | Room 2.6 + DataStore |
| Ağ | Retrofit 2.11 + OkHttp 4.12 |
| Firebase | Auth + Firestore + Analytics |
| Medya | Media3 (ExoPlayer) 1.4 |
| Görüntü | Coil Compose 2.7 |
| OCR | ML Kit Text Recognition + CameraX |
| Serialization | kotlinx-serialization |
| Test | JUnit, MockK, Turbine, Espresso |

## 🧱 Base Sınıflar

### BaseViewModel
```kotlin
abstract class BaseViewModel<S : UiState, E : UiEvent>(initialState: S) : ViewModel() {
    val uiState: StateFlow<S>
    val events: Flow<E>
    protected fun updateState(reducer: S.() -> S)
    protected fun sendEvent(event: E)
    protected fun launch(block: suspend () -> Unit)
}
```

### BaseRepository
```kotlin
abstract class BaseRepository {
    protected suspend fun <T> safeCall(call: suspend () -> T): Resource<T>
    protected suspend fun <T> safeCallOnIo(call: suspend () -> T): Resource<T>
}
```

### Resource
```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
```

## ♿ Erişilebilirlik

- Tüm etkileşimli öğeler `contentDescription` ile TalkBack uyumlu
- WCAG AAA dokunma hedefleri (min 56dp)
- `semantics { heading() }` ile başlık hiyerarşisi
- `liveRegion` ile dinamik içerik duyuruları
- Yüksek kontrast tema desteği
- Ayarlanabilir yazı boyutu (0.8x – 2.0x)

## 🗺️ Navigasyon

```
AUTH_GRAPH → Login → Register
         ↓ (başarılı giriş)
HOME → Reader, Media, OCR, Currency, Converter, Downloader
SETTINGS → Çıkış → AUTH_GRAPH
```

Alt çubuk sekmeleri: Ana Sayfa, Okuyucu, Medya, Ayarlar

## 🚀 Kurulum

1. Repoyu klonlayın:
   ```bash
   git clone https://github.com/vedatguldu/VoxAble-VA.git
   ```
2. `google-services.json` dosyasını `app/` altına yerleştirin
3. Android Studio’da projeyi açın
4. Gradle sync yapın
5. Çalıştırın

## 📋 Bağımlılık Grafiği

```
app → tüm core + tüm feature modülleri
feature-auth → core, core-ui, core-accessibility, core-database, Firebase
feature-home → core, core-ui, core-accessibility
feature-reader → core, core-ui, core-accessibility
feature-media → core, core-ui, core-accessibility, Media3
feature-ocr → core, core-ui, core-accessibility, CameraX, ML Kit
feature-currency → core, core-ui, core-accessibility, core-network
feature-converter → core, core-ui, core-accessibility
feature-downloader → core, core-ui, core-accessibility, core-network, core-database
feature-settings → core, core-ui, core-accessibility, core-database, Firebase Auth
```

## 🤝 Katkıda Bulunma

Katkı rehberi için [CONTRIBUTING.md](CONTRIBUTING.md) dosyasına bakın.

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır.

---

**VoxAble** — Herkes için erişilebilir teknoloji. ♥
