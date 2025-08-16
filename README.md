# HelloGoodbye Language Learning App

A language learning app that helps users practice basic phrases in multiple languages through interactive exercises.

## Features

### Main Screen
- **Language Grid**: Display selected languages with flag icons
- **Currency System**: Earn coins by practicing, spend them to unlock new languages
- **Language Purchase**: Buy new languages for 100 coins each

### Practice Mode
- **Interactive Exercises**: Match audio to flags, pronunciation to flags, audio to English, and pronunciation to English
- **Multi-language Support**: Practice with all your selected languages
- **Progress Tracking**: Complete 4 exercises to finish a practice session
- **Coin Rewards**: Earn 1 coin for each perfect practice run

### Travel Mode (NEW!)
- **Quest-like Progression**: Complete language sections in sequence
- **Section Order**:
  1. **Italy** 🇮🇹 - Italian language practice
  2. **France** 🇫🇷 - French language practice  
  3. **Mixed Section 1** 🌍 - Spanish + German
  4. **Mixed Section 2** 🌍 - Portuguese + Russian
  5. **Mixed Section 3** 🌍 - Japanese + Korean
  6. **Mixed Section 4** 🌍 - Chinese + Dutch
  7. **Mixed Section 5** 🌍 - Swedish + English
- **Progressive Unlocking**: Each section must be completed before the next becomes available
- **Mixed Language Practice**: Later sections combine multiple languages for advanced practice
- **Bonus Feature**: After completing all sections, a plus button appears for free language selection

## Supported Languages

The app supports the following languages with audio pronunciation:
- 🇺🇸 English (en)
- 🇪🇸 Spanish (es) 
- 🇫🇷 French (fr)
- 🇩🇪 German (de)
- 🇮🇹 Italian (it)
- 🇵🇹 Portuguese (pt)
- 🇷🇺 Russian (ru)
- 🇯🇵 Japanese (ja)
- 🇰🇷 Korean (ko)
- 🇨🇳 Chinese Simplified (zh-cn)
- 🇳🇱 Dutch (nl)
- 🇸🇪 Swedish (sv)

## How to Use

1. **Start Learning**: Select languages from the main grid
2. **Practice**: Use the PRACTICE button to improve your skills
3. **Travel**: Use the TRAVEL button to progress through language sections
4. **Earn Coins**: Complete practice sessions perfectly to earn coins
5. **Unlock More**: Spend coins to add new languages to your collection

## Technical Details

- Built with Jetpack Compose
- Audio files stored in app assets
- Language corpus data in JSON format
- Responsive grid layouts for different screen sizes
- State management with Compose state hoisting

## Development

The app is structured with:
- `MainActivity.kt` - Main app logic and UI components
- `corpus.json` - Language data and audio file references
- `audio_files/` - MP3 pronunciation files for each language
- Material Design 3 theming and components
