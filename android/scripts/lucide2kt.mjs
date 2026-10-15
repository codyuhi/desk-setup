// Converts lucide (vanilla or React) icon nodes into Compose ImageVector definitions (stroke icons, 24×24).
import { pathToFileURL } from 'node:url';
const dir = process.argv[2];
const names = process.argv.slice(3);
const pascal = (n) => n.split('-').map((p) => p[0].toUpperCase() + p.slice(1)).join('');
const num = (v) => Number(v);
function toPath([tag, a]) {
  switch (tag) {
    case 'path': return a.d;
    case 'circle': { const cx = num(a.cx), cy = num(a.cy), r = num(a.r);
      return `M${cx - r} ${cy}a${r} ${r} 0 1 0 ${2 * r} 0a${r} ${r} 0 1 0 ${-2 * r} 0`; }
    case 'ellipse': { const cx = num(a.cx), cy = num(a.cy), rx = num(a.rx), ry = num(a.ry);
      return `M${cx - rx} ${cy}a${rx} ${ry} 0 1 0 ${2 * rx} 0a${rx} ${ry} 0 1 0 ${-2 * rx} 0`; }
    case 'line': return `M${a.x1} ${a.y1}L${a.x2} ${a.y2}`;
    case 'polyline': case 'polygon': { const p = a.points.trim().split(/[\s,]+/);
      let d = `M${p[0]} ${p[1]}`; for (let i = 2; i < p.length; i += 2) d += `L${p[i]} ${p[i + 1]}`; return d + (tag === 'polygon' ? 'Z' : ''); }
    case 'rect': { const x = num(a.x ?? 0), y = num(a.y ?? 0), w = num(a.width), h = num(a.height), r = num(a.rx ?? 0);
      if (!r) return `M${x} ${y}h${w}v${h}h${-w}Z`;
      return `M${x + r} ${y}h${w - 2 * r}a${r} ${r} 0 0 1 ${r} ${r}v${h - 2 * r}a${r} ${r} 0 0 1 ${-r} ${r}h${-(w - 2 * r)}a${r} ${r} 0 0 1 ${-r} ${-r}v${-(h - 2 * r)}a${r} ${r} 0 0 1 ${r} ${-r}Z`; }
    default: throw new Error('unsupported ' + tag);
  }
}
let out = `// Generated from lucide ${process.env.LUCIDE_VERSION} (ISC licence) so the app uses the same
// icons as the web client. Regenerate with android/scripts/lucide2kt.mjs; do not edit by hand.
package online.codyuhi.desksetup.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun lucide(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach {
            addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object Lucide {
`;
for (const n of names) {
  const mod = await import(pathToFileURL(`${dir}/${n}.mjs`).href);
  const node = mod.__iconData?.node ?? mod.default;
  const paths = node.map(toPath).map((d) => JSON.stringify(d));
  out += `    val ${pascal(n)}: ImageVector by lazy {\n        lucide(\n            "${n}",\n${paths.map((p) => `            ${p},`).join('\n')}\n        )\n    }\n`;
}
out += '}\n';
process.stdout.write(out);
