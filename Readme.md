## Интеграция Android Wear OS c мобильным устройством на Android

Задача: создать прототип приложения, которое позволяло бы получать частоту сердцебиения с часов на Wear OS и передавать эти данные на смартфон под управлением Android.

Подготовительные работы:

* Создать эмулятор с предустановленными Google Play сервисами
* Установить на эмулятор приложение из Google Play Wear OS (и с не официальных источников https://apkpure.com/wear-os-by-google-smartwatch/com.google.android.wearable.app/download)
* Создать эмулятор для Wear OS (у меня в примере использовался API 30)
* Выполнить сопряжение, используя следующую статью https://developer.android.com/training/wearables/get-started/connect-phone

* Результат: будут созданы два приложения, один для часов, другой для смартфона и при этом из package name должен быть одинаковым. А судя из статьи https://stackoverflow.com/questions/48921165/syncing-data-items-between-mobile-device-and-wear ещё и подписи.

Подробнее https://appcode.mobwal.com/?p=3421