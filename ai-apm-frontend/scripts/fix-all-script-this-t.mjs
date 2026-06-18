import fs from 'fs';
import path from 'path';

function walk(dir, acc = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['node_modules', 'dist', 'generated'].includes(entry.name)) continue;
      walk(p, acc);
    } else if (/\.vue$/.test(entry.name)) {
      acc.push(p);
    }
  }
  return acc;
}

function fixVueFile(file) {
  let content = fs.readFileSync(file, 'utf8');
  if (!content.includes('this.$t(')) return false;

  let changed = false;
  content = content.replace(/<script([^>]*)>([\s\S]*?)<\/script>/g, (full, attrs, body) => {
    if (!body.includes('this.$t(')) return full;
    const fixedBody = body.replace(/this\.\$t\(/g, 'i18n.t(');
    if (fixedBody === body) return full;
    changed = true;

    let nextBody = fixedBody;
    if (!nextBody.includes("from '@/i18n'") && !nextBody.includes('from "@/i18n"')) {
      const importMatch = nextBody.match(/^import .+?;\n/m);
      if (importMatch) {
        const insertAt = importMatch.index + importMatch[0].length;
        nextBody = nextBody.slice(0, insertAt) + "import i18n from '@/i18n';\n" + nextBody.slice(insertAt);
      } else {
        nextBody = "import i18n from '@/i18n';\n" + nextBody;
      }
    }
    return `<script${attrs}>${nextBody}</script>`;
  });

  if (!changed) return false;
  fs.writeFileSync(file, content);
  return true;
}

const fixed = [];
for (const file of walk('src')) {
  if (fixVueFile(file)) fixed.push(file);
}

console.log(`Replaced this.$t -> i18n.t in ${fixed.length} vue files`);
