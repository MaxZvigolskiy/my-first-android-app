# LW IR Remote

Готовое нативное Android-приложение для передачи двух заранее заданных ИК-команд через встроенный IR blaster.

## Команды

1. **СТАРТ ИГРЫ**
   - `83 05 E8`
   - 37 кГц
   - одна передача на одно нажатие

2. **УБИТИЕ / 100 HP**
   - PLAYER_ID = 23
   - TEAM_ID = 2
   - DAMAGE_ID = 15
   - 50 кГц
   - одна передача на одно нажатие

## GitHub Actions

Workflow уже находится здесь:

`.github/workflows/build.yml`

Он:
1. устанавливает JDK 17;
2. устанавливает Gradle 8.9;
3. собирает `assembleDebug`;
4. проверяет наличие APK;
5. загружает APK как Artifact.

### Запуск

На GitHub открой:

**Actions → Build APK → Run workflow**

После завершения:

**Build APK → Artifacts → LW-IR-Remote-APK**

Внутри будет:

`app-debug.apk`

## Локальная сборка

Проект можно открыть в Android Studio.

Для локальной сборки:

`gradle assembleDebug`

## Важно

Приложение использует `ConsumerIrManager`.

Одно нажатие кнопки = одна передача.
Автоматических повторов нет.
