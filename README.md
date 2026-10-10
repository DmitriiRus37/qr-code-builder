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

## How a QR code is built

```
QrCreator.of(text, level).build()
 ├─ 1. choose the mode       numeric ⊂ alphanumeric ⊂ byte (UTF-8)
 ├─ 2. choose the version    smallest V with 4 + count bits + data bits ≤ capacity
 └─ createQr()
     ├─ 3. encode the data       mode indicator + character count + data bits
     ├─ 4. finish the bit stream terminator → pad to a byte → pad bytes 0xEC 0x11
     ├─ 5. error correction      split into blocks → Reed–Solomon per block → interleave
     ├─ 6. function patterns     finders + separators, timing, alignment, reserved areas
     ├─ 7. place the data        two-column zig-zag from the bottom-right corner
     ├─ 8. mask                  try all 8 masks, score N1–N4, keep the lowest; write format info
     └─ 9. version information   18 bits, V7 and above only
QrImageDrawer                    PNG or SVG, scale × modules, quiet zone
```

The three examples below are traced through the real code (`1234`, `HELLO WORLD` and `Хаб`, all at level M).
All three end up in version 1. What changes from version to version is collected in the
[per-version reference](#per-version-reference) at the end.

### 1. Choose the mode

| Mode         | Indicator | Characters                                   | Packing                                    |
|--------------|-----------|----------------------------------------------|--------------------------------------------|
| Numeric      | `0001`    | `0-9`                                        | groups of 3 digits → 10 bits (2 → 7, 1 → 4) |
| Alphanumeric | `0010`    | `0-9 A-Z`, space, `$ % * + - . / :` (45 chars) | pairs → 45·a + b in 11 bits (single → 6)    |
| Byte         | `0100`    | anything (text as UTF-8)                     | 8 bits per byte                            |

The most compact mode that covers every character wins: `1234` is numeric, `HELLO WORLD` is alphanumeric,
`Хаб` is byte (Cyrillic is in neither of the other sets).

### 2. Choose the version

The character-count field is 10 / 9 / 8 bits (numeric / alphanumeric / byte) in V1–V9 and 12 / 11 / 16 bits in V10.
The encoder takes the smallest version where *4 (mode) + count bits + data bits* fits the data capacity for the
level (V1-M: 16 data codewords = 128 bits):

| Text          | Mode         | Bits needed                    | Version |
|---------------|--------------|--------------------------------|---------|
| `1234`        | numeric      | 4 + 10 + (10 + 4) = **28**     | V1-M    |
| `HELLO WORLD` | alphanumeric | 4 + 9 + (5·11 + 6) = **74**    | V1-M    |
| `Хаб`         | byte         | 4 + 8 + 6·8 = **60**           | V1-M    |

### 3. Encode the data

**Number `1234` (numeric):**
1. Split into groups of three digits: `123` and `4`.
2. Each group in binary, its width set by the number of digits: `123` → `0001111011` (10 bits), `4` → `0100` (4 bits).
   A group with leading zeros keeps its width (`012` still takes 10 bits).
3. In front: the mode indicator `0001` and the character count 4 in 10 bits → `0000000100`.

```
0001 | 0000000100 | 0001111011 | 0100                      → 28 bits
```

**Text `HELLO WORLD` (alphanumeric):** every character has a value (`H`=17, `E`=14, … space=36). Pairs become
45·first + second in 11 bits, the last single character takes 6 bits: `HE` → 45·17 + 14 = 779 → `01100001011`, …, `D` → 13 → `001101`.

```
0010 | 000001011 | 01100001011 01111000110 10001011100 10110111000 10011010100 | 001101   → 74 bits
```

**Text `Хаб` (byte):**
1. The text is turned into UTF-8 bytes: `D0 A5 D0 B0 D0 B1`. Each Cyrillic letter takes 2 bytes.
2. Each byte is written in exactly 8 bits.
3. In front: the mode indicator `0100` and the count, which counts **bytes** (6), not letters, in 8 bits → `00000110`.

```
0100 | 00000110 | 11010000 10100101 11010000 10110000 11010000 10110001   → 60 bits
```

### 4. Finish the bit stream

1. **Terminator:** up to 4 zero bits (fewer if the capacity is already reached).
2. **Pad to a byte:** zero bits until the length is a multiple of 8.
3. **Pad bytes:** `236` and `17` (`11101100`, `00010001`) alternately until the data capacity is full.

For `1234` at V1-M: 28 bits + `0000` = 32 bits = 4 codewords `16 16 123 64`, then 12 pad bytes:

```
data codewords: 16 16 123 64 236 17 236 17 236 17 236 17 236 17 236 17
```

### 5. Error correction

1. **Blocks.** The data codewords are split into blocks (group 1, and in larger versions a group 2 whose blocks hold
   one codeword more). V1-M has a single block of 16. V5-Q, for example, has 2 blocks of 15 and 2 blocks of 16.
2. **Reed–Solomon.** Each block gets its own error-correction codewords: the remainder of data(x)·xⁿ divided by the
   generator polynomial g(x) = (x − α⁰)(x − α¹)…(x − αⁿ⁻¹) over GF(256) (primitive polynomial `0x11D`). V1-M uses
   n = 10. The generator is stored as α-exponents (`GeneratingPolynomialMap`), and the GF(256) log/exp tables are
   computed in `GaloisField`.

   ```
   1234:        EC 196 248 211 253 161 167 111 199 207 72
   HELLO WORLD: EC 196 35 39 119 235 215 231 226 93 23     (the thonky example)
   Хаб:         EC 31 142 32 78 149 145 253 26 215 172
   ```
3. **Interleave.** The final sequence takes the 1st data codeword of every block, then the 2nd of every block, and so
   on; after that the EC codewords in the same way. With one block (V1-M) this is just data followed by EC:
   26 codewords = 208 bits for `1234`.

### 6. Draw the function patterns

On an empty N×N grid (N = 17 + 4·version):
- **Finder patterns:** 7×7 squares in three corners, each with a one-module light separator.
- **Timing patterns:** alternating dark and light modules along row 6 and column 6.
- **Alignment patterns** (V2 and above): 5×5 squares, centred on every pair of the version's centre coordinates
  except the three pairs that overlap the finders. V2–V6 have 1, V7–V10 have 6.
- **Reserved areas:** the 2 × 15 format-information modules around the finders plus the always-dark module at
  (4·version + 9, 8), and from V7 the two 6×3 version-information blocks.

All these modules are marked busy. Data never goes there, and the mask never touches them.

### 7. Place the data

The bit stream fills the free modules in two-column strips from the bottom-right corner, zig-zagging up and down
and skipping the vertical timing column. Busy modules are skipped. If modules are left over (7 remainder bits in
V2–V6), they stay light (0), as the spec requires.

### 8. Choose the mask and write the format information

Each of the 8 masks flips the data modules where its condition holds (i = row, j = column), for example mask 0
`(i + j) % 2 == 0` or mask 2 `j % 3 == 0`. Each candidate is scored with the penalty rules:

| Rule | Penalty |
|------|---------|
| N1 | every run of 5 or more same-coloured modules in a row or column: 3 + (length − 5) |
| N2 | every 2×2 block of one colour: 3 |
| N3 | every finder-like pattern `1011101` with 4 light modules on one side: 40 |
| N4 | 10 for every full 5 % the share of dark modules is away from 50 % |

The mask with the lowest total wins (a tie goes to the lower number). Then the **format information** is written
twice: 15 bits = 2 bits of EC level + 3 bits of mask + 10 BCH bits, XORed with `101010000010010`. Here the chosen
masks are 2 for `1234` (format `101111001111100`) and 0 for `HELLO WORLD` and `Хаб` (format `101010000010010`).

### 9. Version information (V7 and above)

18 bits = the 6-bit version number + 12 BCH bits (generator `0x1F25`), written into both 6×3 blocks: above the
bottom-left finder and left of the top-right finder. For example V7 = `000111110010010100`. V1–V6 have none.

### Per-version reference

| V  | Size  | Alignment centres | Count bits N/A/B | Remainder bits | L blocks × data, EC | M blocks × data, EC | Q blocks × data, EC | H blocks × data, EC |
|----|-------|-------------------|------------------|----------------|---------------------|---------------------|---------------------|---------------------|
| 1  | 21×21 | none              | 10/9/8   | 0 | 1×19, 7         | 1×16, 10         | 1×13, 13         | 1×9, 17          |
| 2  | 25×25 | 6, 18             | 10/9/8   | 7 | 1×34, 10        | 1×28, 16         | 1×22, 22         | 1×16, 28         |
| 3  | 29×29 | 6, 22             | 10/9/8   | 7 | 1×55, 15        | 1×44, 26         | 2×17, 18         | 2×13, 22         |
| 4  | 33×33 | 6, 26             | 10/9/8   | 7 | 1×80, 20        | 2×32, 18         | 2×24, 26         | 4×9, 16          |
| 5  | 37×37 | 6, 30             | 10/9/8   | 7 | 1×108, 26       | 2×43, 24         | 2×15 + 2×16, 18  | 2×11 + 2×12, 22  |
| 6  | 41×41 | 6, 34             | 10/9/8   | 7 | 2×68, 18        | 4×27, 16         | 4×19, 24         | 4×15, 28         |
| 7  | 45×45 | 6, 22, 38         | 10/9/8   | 0 | 2×78, 20        | 4×31, 18         | 2×14 + 4×15, 18  | 4×13 + 1×14, 26  |
| 8  | 49×49 | 6, 24, 42         | 10/9/8   | 0 | 2×97, 24        | 2×38 + 2×39, 22  | 4×18 + 2×19, 22  | 4×14 + 2×15, 26  |
| 9  | 53×53 | 6, 26, 46         | 10/9/8   | 0 | 2×116, 30       | 3×36 + 2×37, 22  | 4×16 + 4×17, 20  | 4×12 + 4×13, 24  |
| 10 | 57×57 | 6, 28, 50         | 12/11/16 | 0 | 2×68 + 2×69, 18 | 4×43 + 1×44, 26  | 6×19 + 2×20, 24  | 6×15 + 2×16, 28  |

"2×15 + 2×16, 18" means 2 blocks of 15 data codewords and 2 blocks of 16, each with 18 EC codewords. Version
information (step 9) is present from V7. The total number of codewords is the same for every level of a version
(26 for V1 … 346 for V10); a higher level trades data codewords for EC codewords.

### Code map

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
