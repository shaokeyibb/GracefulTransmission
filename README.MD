# GracefulTransmission

Provide amazing teleporting animation same as the GTA Online game.

![](docs/demo.gif)

## Feature
- [x] Teleporting animation
- [x] Not affect other players
- [x] Fully customizable

## How to use
1. Install ProtocolLib plugin.
2. Download the latest release of the plugin.
3. Put the plugin into your `plugins` folder.
4. Restart your server.
5. Enjoy!

## Configuration
```yaml
# Durations in ticks
general:
  # The duration of the raising up animation.
  up-duration: 40
  # The duration between the x-z translate animation and the previous animation.
  fade-in-duration: 20
  # The duration of the x-z translate animation.
  stay-duration: 20
  # The duration between the x-z translate animation and the next animation.
  fade-out-duration: 40
  # The duration of the falling down animation.
  down-duration: 40
  # The idle duration after the falling down animation.
  down-stay-duration: 20
  # The block to raise up when teleporting.
  up-offset: 100.0
  # Whether to switch the weather to clear when teleporting.
  modify-weather: true
  # Days visualized elapsed in the world after teleporting.
  day-cycle: 1
```

## License
The program is licensed under [The MIT License](LICENSE).