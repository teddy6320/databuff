import fs from 'fs';
import path from 'path';

function walk(dir, acc = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['node_modules', 'dist', 'generated'].includes(entry.name)) continue;
      walk(p, acc);
    } else if (file.endsWith('.vue') && !entry.name.endsWith('.d.ts')) {
      acc.push(p);
    } else if (/\.vue$/.test(entry.name)) {
      acc.push(p);
    }
  }
  return acc;
}

// fix walk bug
function walk2(dir, acc = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['node_modules', 'dist', 'generated'].includes(entry.name)) continue;
      walk2(p, acc);
    } else if (/\.vue$/.test(entry.name)) {
      acc.push(p);
    }
  }
  return acc;
}

const hits = [];
for (const file of walk2('src')) {
  const content = fs.readFileSync(file, 'utf8');
  const m = content.match(/<script[^>]*>([\s\S]*?)<\/script>/);
  if (!m) continue;
  const body = m[1];
  const classMatch = body.match(/export\s+default\s+class[\s\S]*/);
  if (!classMatch) continue;
  const classBody = classMatch[0];
  const fieldInit = classBody.match(/(?:private|public|protected)\s+\w+[^=]*=\s*[\[{][\s\S]*?this\.\$t\(/);
  if (fieldInit) hits.push(file);
}

console.log('Class field initializer this.$t:', hits.length);
hits.slice(0, 40).forEach((f) => console.log(f));
if (hits.length > 40) console.log('...', hits.length - 40, 'more');
