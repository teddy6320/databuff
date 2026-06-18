import fs from 'fs';
import path from 'path';

function walk(dir, acc = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['node_modules', 'dist', 'generated'].includes(entry.name)) continue;
      walk(p, acc);
    } else if (/\.(vue|ts|js)$/.test(entry.name) && !entry.name.endsWith('.d.ts')) {
      acc.push(p);
    }
  }
  return acc;
}

const hits = [];
for (const file of walk('src')) {
  const content = fs.readFileSync(file, 'utf8');
  if (!content.includes('this.$t(')) continue;

  const scripts = [...content.matchAll(/<script[^>]*>([\s\S]*?)<\/script>/g)].map((m) => m[1]);
  const bodies = scripts.length ? scripts : [content];

  for (const body of bodies) {
    const classIdx = body.search(/export\s+default\s+class|@Component/);
    const preClass = classIdx >= 0 ? body.slice(0, classIdx) : '';
    if (!/this\.\$t\(/.test(preClass)) continue;

    const lines = preClass
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.includes('this.$t('));

    hits.push({ file, lines });
  }
}

console.log(`Module-level this.$t files: ${hits.length}`);
for (const hit of hits) {
  console.log(`\n${hit.file}`);
  for (const line of hit.lines.slice(0, 8)) {
    console.log(`  ${line.slice(0, 140)}`);
  }
  if (hit.lines.length > 8) console.log(`  ... +${hit.lines.length - 8} more`);
}
