# Katkıda Bulunma Kılavuzu

VoxAble projesine katkıda bulunmak istediğiniz için teşekkür ederiz! 🌟

## Başlangıç

1. Repoyu fork'layın
2. Yeni bir branch oluşturun: `git checkout -b feature/yenilik-adi`
3. Değişikliklerinizi yapın
4. Commit atın: `git commit -m "feat: yenilik açıklaması"`
5. Push edin: `git push origin feature/yenilik-adi`
6. Pull Request açın

## Commit Mesajı Kuralları

[Conventional Commits](https://www.conventionalcommits.org/) standardını takip ediyoruz:

- `feat:` — Yeni özellik
- `fix:` — Hata düzeltmesi
- `docs:` — Dokümantasyon
- `refactor:` — Yeniden düzenleme
- `test:` — Test ekleme/düzeltme
- `build:` — Build sistemi değişiklikleri
- `chore:` — Diğer değişiklikler

## Kod Standartları

- **Kotlin** coding conventions’ı takip edin
- Tüm etkileşimli Compose bileşenlere `contentDescription` ekleyin
- Minimum dokunma hedefi: **56dp** (WCAG AAA)
- Türkçe UI metinleri için Türkçe karakter desteği zorunlu (ç, ğ, ı, İ, ö, ş, ü)
- Feature modüllerinde MVI pattern kullanın (Contract + ViewModel + Screen)

## Erişilebilirlik

Bu proje görme engelli kullanıcılar için tasarlanmıştır. Tüm değişiklikler:

- TalkBack ile test edilmelidir
- Semantik etiketler içermelidir
- Heading hiyerarşisini korumalıdır
- Dinamik içerik duyurularını desteklemelidir

## Modül Yapısı

Yeni özellik eklerken:

1. `feature-xxx/` altında yeni modül oluşturun
2. `settings.gradle.kts` dosyasına modülü ekleyin
3. MVI pattern kullanın: `navigation/`, `domain/`, `data/`, `di/`, `presentation/`
4. `build.gradle.kts` içinde gerekli core bağımlılıkları ekleyin

## Lisans

Katkılarınız MIT Lisansı altında yayınlanacaktır.
