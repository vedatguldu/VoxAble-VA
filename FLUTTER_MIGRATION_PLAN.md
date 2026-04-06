# VoxAble Flutter Migration - Kalici Plan

Bu dosya sohbet kapanmasina karsi kalici referans olarak tutulur.
Ana plan kaynagi: /memories/session/plan.md
Son guncelleme: 2026-04-07

## Kilit Kararlar (Sabit)
- Flutter proje kimligi: com.voxable.app
- Paket politikasi: sadece ucretsiz/acik kaynak
- Arka plan stratejisi: fallback zorunlu (background + foreground/manual sync hibrit)
- Teslim modeli: big-bang, tum moduller kapsamda
- Kalite yaklasimi: Android testleri her fazda zorunlu, iOS VoiceOver iOS fazinda release gate

## Kapsam
- Android Kotlin VoxAble uygulamasinin tum feature seti Flutter ile sifirdan yeniden yazilacak.
- Hedef platformlar: Android + iOS
- Mimari hedef: uretim kalitesinde, test edilebilir, erisilebilirlik odakli yapi

## Fazlar (Ozet)
1. Altyapi ve proje kurulumu (DI, router, l10n, Firebase, CI)
2. Core katmanlari (network, database, sync, error/base)
3. Core UI + Accessibility
4. Feature'lar: auth, home, settings, reader, media, ocr, currency, converter, downloader
5. Test ve kalite guvence (unit + widget + integration + analyze)
6. Dagitim (Android + iOS store surecleri)

## Kritik Riskler ve Zorunlu Onlemler
- iOS background execution deterministik degil: manuel/foreground sync fallback mecburi
- Drift schema degisikliklerinde migration testleri zorunlu
- Dokuman parser (DAISY/FB2/CBZ) gerekirse custom implementasyon
- Platform farklari sadece entegrasyon/izin/build-signing katmaninda ele alinir

## Guncelleme Kurali
- Yeni karar, degisiklik veya kapsam eklendikce bu dosya guncellenir.
- Detayli adimlar ve teknik dagilim icin /memories/session/plan.md esas alinir.
