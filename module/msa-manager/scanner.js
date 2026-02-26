import fs from 'fs';
import path from 'path';
import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const xml2js = require('xml2js');
const yaml = require('js-yaml');

const ROOT = path.resolve('/opt/carbosys');

// Read application.yml and extract port & app name
function parseAppYml(dir) {
    const possiblePaths = [
        path.join(dir, 'src', 'main', 'resources', 'application.yml'),
        path.join(dir, 'src', 'main', 'resources', 'application.yaml'),
    ];
    for (const p of possiblePaths) {
        if (fs.existsSync(p)) {
            try {
                const doc = yaml.load(fs.readFileSync(p, 'utf8'));
                const port = doc?.server?.port ?? null;
                const appName = doc?.spring?.application?.name ?? null;
                return { port, appName };
            } catch { /* ignore */ }
        }
    }
    return { port: null, appName: null };
}

// Read pom.xml and extract artifactId
async function parsePom(dir) {
    const pomPath = path.join(dir, 'pom.xml');
    if (!fs.existsSync(pomPath)) return null;
    try {
        const xml = fs.readFileSync(pomPath, 'utf8');
        const result = await xml2js.parseStringPromise(xml, { explicitArray: false });
        const proj = result?.project;
        if (!proj) return null;
        return {
            artifactId: proj.artifactId ?? proj.name ?? null,
            groupId: proj.groupId ?? null,
        };
    } catch { return null; }
}

export async function scanModules() {
    const entries = fs.readdirSync(ROOT, { withFileTypes: true });
    const modules = [];

    for (const entry of entries) {
        if (!entry.isDirectory()) continue;
        const dir = path.join(ROOT, entry.name);
        const pomPath = path.join(dir, 'pom.xml');
        // Only consider directories with both pom.xml and src/main
        if (!fs.existsSync(pomPath)) continue;
        if (!fs.existsSync(path.join(dir, 'src', 'main'))) continue;

        const [pomInfo, { port, appName }] = await Promise.all([
            parsePom(dir),
            Promise.resolve(parseAppYml(dir)),
        ]);

        if (!pomInfo) continue;

        modules.push({
            id: entry.name,
            name: appName ?? pomInfo.artifactId ?? entry.name,
            dir,
            port,
            artifactId: pomInfo.artifactId,
            status: 'stopped',
            pid: null,
            logs: [],
        });
    }

    return modules;
}
