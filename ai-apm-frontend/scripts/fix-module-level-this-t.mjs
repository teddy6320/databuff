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

function fixVueOrTsFile(file) {
  let content = fs.readFileSync(file, 'utf8');
  if (!content.includes('this.$t(')) return false;

  const isVue = file.endsWith('.vue');
  let changed = false;

  const fixSection = (section) => {
    const classIdx = section.search(/export\s+default\s+class|@Component/);
    if (classIdx < 0) return section;

    const pre = section.slice(0, classIdx);
    const rest = section.slice(classIdx);
    if (!/this\.\$t\(/.test(pre)) return section;

    const fixedPre = pre.replace(/this\.\$t\(/g, 'i18n.t(');
    if (fixedPre === pre) return section;
    changed = true;
    return fixedPre + rest;
  };

  if (isVue) {
    content = content.replace(/<script([^>]*)>([\s\S]*?)<\/script>/g, (full, attrs, body) => {
      const fixedBody = fixSection(body);
      if (fixedBody === body) return full;
      return `<script${attrs}>${fixedBody}</script>`;
    });
  } else {
    const fixed = fixSection(content);
    if (fixed !== content) {
      content = fixed;
      changed = true;
    }
  }

  if (!changed) return false;

  // Add i18n import in script if missing
  if (isVue) {
    content = content.replace(/<script([^>]*)>([\s\S]*?)<\/script>/, (full, attrs, body) => {
      if (body.includes("from '@/i18n'") || body.includes('from "@/i18n"')) {
        return full;
      }
      const importMatch = body.match(/^import .+?;\n/m);
      if (importMatch) {
        const insertAt = importMatch.index + importMatch[0].length;
        body = body.slice(0, insertAt) + "import i18n from '@/i18n';\n" + body.slice(insertAt);
      } else {
        body = "import i18n from '@/i18n';\n" + body;
      }
      return `<script${attrs}>${body}</script>`;
    });
  } else if (!content.includes("from '@/i18n'") && !content.includes('from "@/i18n"')) {
    const importMatch = content.match(/^import .+?;\n/m);
    if (importMatch) {
      const insertAt = importMatch.index + importMatch[0].length;
      content = content.slice(0, insertAt) + "import i18n from '@/i18n';\n" + content.slice(insertAt);
    } else {
      content = "import i18n from '@/i18n';\n" + content;
    }
  }

  // Fix @Prop({ default: this.$t -> default: () => i18n.t
  content = content.replace(
    /@Prop\(\{\s*default:\s*i18n\.t\(/g,
    '@Prop({ default: () => i18n.t('
  );

  fs.writeFileSync(file, content);
  return true;
}

const fixed = [];
for (const file of walk('src')) {
  if (fixVueOrTsFile(file)) fixed.push(file);
}

console.log(`Fixed ${fixed.length} files:`);
fixed.forEach((f) => console.log(f));
