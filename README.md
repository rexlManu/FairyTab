# FairyTab

The ultimate tablist manager for your server.

*FairyTab is the forefront of tablist management, offering a plethora of features from dynamic prefixes to intricate
sorting capabilities. The ideal tool to deliver a polished and immersive tablist experience for your server members.*

## Features

- **Plug and play**: Zero configuration required.
- **Lightweight**: Only the features you need.
- **LuckPerms support**: Tailor your tablist based on user groups, and set individualized prefixes, colors, and
  suffixes.

## LuckPerms Configuration

FairyTab auto-fetches necessary data from LuckPerms.

### The hierarchy for prefixes is:

- User's `tab-prefix` meta value
- User's `prefix` meta value
- Group's `tab-prefix` meta value
- Group's `prefix` meta value

*The same hierarchy applies for suffixes, using the tab-suffix and suffix meta values respectively.*

### Sorting

Group-based sorting is determined by group weight.

### Team Color

Team color is determined by the user's or group's `tab-color` meta value.

You find all available color values [here](https://docs.advntr.dev/minimessage/format.html#color).

Default is `gray`.

## Installation

1. Download the latest version from the [releases page](https://github.com/rexlManu/FairyTab/releases/latest)
2. Put the plugin into your plugins folder
3. Restart your server

## Other permission systems

If you are using another permission system, feel free to open an issue for it. I will try to add support for it as soon
as possible.

## Support

If you need any help with FairyTab, feel free to join our [Discord server](https://discord.gg/bM8NtsJVeb).

For bug reports and feature requests, please open an issue on [GitHub](https://github.com/rexlManu/FairyTab/issues).

## License

**FairyTab** is licensed under MIT License, which can be found [here](LICENSE).
