# QR Code Builder

An educational QR code **encoder** in plain Java 25. It follows ISO/IEC 18004 and the
[thonky QR code tutorial](https://www.thonky.com/qr-code-tutorial/): every stage of the pipeline is
written out (bit streams, Reed–Solomon over GF(256), data placement, masking), so you can read
how a QR code is built. It has no runtime dependencies.

## Features

- Versions **1–10** (21×21 up to 57×57 modules)
- Error correction levels **L, M, Q, H**
- **Numeric**, **alphanumeric** and **byte** mode. Byte mode writes text as UTF-8 with no ECI header,
  which practically every reader accepts. Raw `byte[]` input is written as it is.
- **Automatic choices**: the most compact mode that covers the text, the smallest version it fits,
  and the mask with the lowest penalty
- All **8 mask patterns**, scored by the penalty rules N1–N4. You can also force one.
- Version information for V7 and above, and format information for the chosen mask
- Output as **PNG** (1-bit, lossless) or **SVG**, with a configurable scale and a **4-module quiet zone** by default
- A command-line interface packaged as a runnable jar

Not supported: versions 11–40, Kanji mode, ECI headers, mixed-mode segments and structured append.

## Capacity (V1–V10)

The most characters each version holds, by level: N = digits (numeric), A = alphanumeric characters, B = bytes.
Alphanumeric means `0-9`, `A-Z` (upper case only), space and `$ % * + - . / :`. Other text goes into byte mode,
where one character can take several bytes in UTF-8 (`"Хаб"` takes 6).

| Version | Size  | L (N / A / B)   | M (N / A / B)   | Q (N / A / B)   | H (N / A / B)   |
|---------|-------|-----------------|-----------------|-----------------|-----------------|
| 1       | 21×21 | 41 / 25 / 17    | 34 / 20 / 14    | 27 / 16 / 11    | 17 / 10 / 7     |
| 2       | 25×25 | 77 / 47 / 32    | 63 / 38 / 26    | 48 / 29 / 20    | 34 / 20 / 14    |
| 3       | 29×29 | 127 / 77 / 53   | 101 / 61 / 42   | 77 / 47 / 32    | 58 / 35 / 24    |
| 4       | 33×33 | 187 / 114 / 78  | 149 / 90 / 62   | 111 / 67 / 46   | 82 / 50 / 34    |
| 5       | 37×37 | 255 / 154 / 106 | 202 / 122 / 84  | 144 / 87 / 60   | 106 / 64 / 44   |
| 6       | 41×41 | 322 / 195 / 134 | 255 / 154 / 106 | 178 / 108 / 74  | 139 / 84 / 58   |
| 7       | 45×45 | 370 / 224 / 154 | 293 / 178 / 122 | 207 / 125 / 86  | 154 / 93 / 64   |
| 8       | 49×49 | 461 / 279 / 192 | 365 / 221 / 152 | 259 / 157 / 108 | 202 / 122 / 84  |
| 9       | 53×53 | 552 / 335 / 230 | 432 / 262 / 180 | 312 / 189 / 130 | 235 / 143 / 98  |
| 10      | 57×57 | 652 / 395 / 271 | 513 / 311 / 213 | 364 / 221 / 151 | 288 / 174 / 119 |

## Build and test

Requirements: JDK 25 or newer, and Maven.

```sh
mvn -B clean verify        # compile and run all tests
mvn -B package             # build target/qr-code-builder.jar (tests included)
```

The tests do not just compare the output with stored snapshots. They decode every generated symbol with two
independent readers, [ZXing](https://github.com/zxing/zxing) and [BoofCV](https://boofcv.org) (both test-scope
only):

- **Round trips:** numeric, alphanumeric and byte (UTF-8) samples on every version × level, plus the capacity
  boundaries of each mode. Each must decode correctly with BoofCV and with ZXing.
- **Spec vectors:** the GF(256) tables, the generator polynomials, thonky's "HELLO WORLD" 1-M codewords,
  the BCH format and version strings, and the capacity table.
- **Cross-check with ZXing's encoder:** the chosen mask and the whole matrix are bit-identical to ZXing's for
  random payloads and for all 8 forced masks.
- **Images and CLI:** PNG and SVG files are written, read back and decoded. The CLI parser and exit codes are tested.

One documented exception: a few correct symbols cannot be located by ZXing's finder-pattern detector
(`QrTestSupport.UNDETECTABLE_BY_ZXING`). For each of them the test checks that ZXing still fails, that the matrix is
bit-identical to ZXing's own encoder, that ZXing decodes the bits in `PURE_BARCODE` mode, and that BoofCV decodes
the symbol normally. So the list can only hide a reader limitation, never an encoder bug.

## Command line

```sh
mvn -B package
java -jar target/qr-code-builder.jar "https://example.com"
# wrote qr.png (V2-M, byte mode, mask 2)
java -jar target/qr-code-builder.jar "HELLO WORLD" -l Q -s 4 -o out/hello.svg
```

`--help` prints:

```
Usage: java -jar qr-code-builder.jar TEXT [options]

Encodes TEXT as a QR code (versions 1-10) and writes it as a PNG or SVG image.

Options:
  -l L|M|Q|H   error correction level (default M)
  -v 1..10     version (default: the smallest that fits)
  -m 0..7      mask pattern (default: the one with the lowest penalty)
  -s 1..50     pixels per module (default 10)
  -q 0..40     quiet zone in modules (default 4)
  -o FILE      output file (default qr.png, or qr.svg with --svg).
               A .png or .svg extension selects the format; without
               an extension, .png (or .svg with --svg) is appended.
               Other extensions, and --svg with a .png file, are
               errors. An existing directory, or a FILE ending in
               '/', means qr.png (or qr.svg) inside that directory.
               Missing directories are created.
  --svg        write SVG instead of PNG
  --debug      also write debug_FILE with the reserved (function)
               modules in dark
  -h, --help   print this help and exit
  --           end of options: every argument after -- is TEXT
               (e.g. a TEXT starting with '-'); put options before it

Exit codes: 0 success; 1 the text cannot be encoded or the file
cannot be written; 2 usage error.
```

Exit codes in detail:

| Code | Meaning |
|------|---------|
| 0    | The image was written (or `--help` was printed). |
| 1    | The text cannot be encoded (for example it does not fit V10, or the version given with `-v`), or the file cannot be written. A one-line `error: ...` message goes to stderr and nothing is written. |
| 2    | Usage error (unknown option, invalid value, missing TEXT). The message and the usage go to stderr. |

## Library use

```java
import educational.dmitriigurylev.QrCodeField;
import educational.dmitriigurylev.QrCreator;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

import java.nio.file.Path;

// automatic mode, version and mask
QrCodeField field = QrCreator.of("HELLO WORLD", CorrectionLevel.MEDIUM).build();
int dark = field.getField()[0][0].getValue();          // the matrix: Cell[row][col], value 1 = dark

// overrides, and the image
QrCreator creator = QrCreator.of("https://example.com", CorrectionLevel.QUARTILE)
        .version(Version.V_4)   // optional: a fixed version (throws if the text does not fit)
        .forceMask(3);          // optional: a fixed mask 0-7 (the spec wants the lowest-penalty one)
creator.build();
creator.drawQr(Path.of("qr.png"));                     // default scale 10 and quiet zone 4
creator.drawer().setScale(4).setQuietZone(6).write(Path.of("qr.svg"));

// raw bytes are always byte mode
QrCreator.of(new byte[]{0x01, 0x02, (byte) 0xFF}, CorrectionLevel.LOW).build();
```

After `build()`, the creator reports what it chose: `getVersion()`, `getEncodingWay()` and `getMaskPattern()`.
Errors are unchecked: `InsufficientCapacityException` when the payload does not fit, and
`InvalidInputFormatException` for empty input or text that is not valid UTF-16 (a lone surrogate).
`QrImageDrawer` also offers `toImage()` (a `BufferedImage`), `toSvg()` (a `String`) and `setDebug(true)`, which
also writes `debug_<name>` with the reserved function modules in dark.

## How it works

```
QrCreator.of(text, level).build()
 ├─ choose the mode       numeric ⊂ alphanumeric ⊂ byte (UTF-8)
 ├─ choose the version    smallest V with 4 + count bits + data bits ≤ capacity
 └─ createQr()
     ├─ function patterns       finders + separators, timing, alignment patterns
     ├─ reserve areas           format information (and version information from V7)
     ├─ encode bits             mode indicator, character count, data (Numeric/Alphanumeric/ByteEncoder)
     │                          → terminator → pad to a byte → pad bytes 0xEC 0x11
     │                          → split into blocks → Reed–Solomon EC per block → interleave
     ├─ place data              two-column zig-zag from the bottom-right corner
     ├─ choose the mask         try all 8, score N1–N4 (MaskEvaluator), keep the lowest
     ├─ apply the mask
     └─ write format and version information
QrImageDrawer                   PNG or SVG, scale × modules, quiet zone
```

| Package                | Contents |
|------------------------|----------|
| `educational.dmitriigurylev` | `QrCreator` (API), `QrCodeField` (matrix and function patterns), `MaskEvaluator`, `QrImageDrawer`, `UtilityMethods` (bit stream and Reed–Solomon), `Main`/`CliOptions` (CLI) |
| `encoders`             | `NumericEncoder`, `AlphanumericEncoder`, `ByteEncoder` |
| `enums`                | `Version`, `CorrectionLevel`, `EncodingWay` (mode), `MaskPattern`, `FinderPatternLocation` |
| `reed_solomon_mapping` | `GaloisField`: exp/log tables of GF(256), computed from the primitive polynomial `0x11D` |
| `utility_maps`         | tables from the spec: capacity, blocks, EC codewords per block, count-indicator widths, mode indicators, format strings, alignment centres, generator polynomials, the alphanumeric character set |
| `custom_exceptions`    | `InsufficientCapacityException`, `InvalidInputFormatException` |

## References

- ISO/IEC 18004:2015, *QR Code bar code symbology specification*
- Thonky QR code tutorial: https://www.thonky.com/qr-code-tutorial/
- Habr article (in Russian): https://habr.com/ru/articles/172525/
- YouTube video: https://www.youtube.com/watch?v=FmghzEXMOrQ&t
- Nayuki, *QR Code generator library*: https://www.nayuki.io/page/qr-code-generator-library
- ZXing: https://github.com/zxing/zxing and BoofCV: https://boofcv.org (test oracles)
