# TrackExchangeButler
TrackExchangeButler is a standalone Java process which interacts with the [TrackExchange Plugin](https://github.com/Pigalala/TrackExchange) for Paper servers.

TrackExchangeButler's main goal is to store trackexchange files and serve them.

## Running
1. Download a jar file from [releases]().
2. Copy the jar file to an empty directory.
3. Run `java -jar <file> <port>`
   - `[file]` is the name of the jar file.
   - `[port]` is the port that the process will bind to.
4. For each track exchange plugin that you wish to connect:
   - Set `butler.enabled = true` in the plugin config.
   - Set `butler.url` in the plugin config. If this process is hosted on the same machine as other trackexchange files, you can probably get away with `http://localhost:[PORT]/`
   - Optionally set `buter.key` in the plugin config. See [Security](#security)

All trackexchange files are stored in `./trackexchange`, relative to the jar file.

## Security
TrackExchangeButler uses user defined keys as authorization. They are currently stored in plaintext in `./keys`.
One could argue that this is silly, but it works for now.

Each line of the keys file represents a key.
If no keys are defined, then authentication is not used.

## Building
*JDK 21 is required*
1. Clone the repository.
2. Run `./gradlew build`
3. The artifact can be found in `./build/libs/`
