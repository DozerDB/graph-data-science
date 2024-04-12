import json
from pathlib import Path

BASE_LINK = "xref:common-usage/running-algos.adoc#"
LINKS = {
    "concurrency": "common-configuration-concurrency",
    "nodeLabels": "common-configuration-node-labels",
    "relationshipTypes": "common-configuration-relationship-types",
    "nodeWeightProperty": "common-configuration-node-weight-property",
    "relationshipWeightProperty": "common-configuration-relationship-weight-property",
    "maxIterations": "common-configuration-max-iterations",
    "tolerance": "common-configuration-tolerance",
    "seedProperty": "common-configuration-seed-property",
    "writeProperty": "common-configuration-write-property",
    "writeConcurrency": "common-configuration-write-concurrency",
    "jobId": "common-configuration-jobid",
    "logProgress": "common-configuration-logProgress",
}

conf_filename = "algorithms-conf.json"
adoc_root = Path("..") / "modules" / "ROOT" / "partials"

with open(conf_filename) as conf_file:
    conf_json = json.load(conf_file)

    for algo in conf_json["algorithms"]:
        adoc_filename = adoc_root / algo["page_path"] / "specific-configuration.adoc"

        if not Path(adoc_filename).exists():
            print(f"File '{adoc_filename}' skipped")
            continue

        with open(adoc_filename, "w") as adoc_file:
            adoc_file.write("// DO NOT EDIT: File generated automatically\n")
            
            for conf in algo["config"]:
                name, type_, default, optional, description = conf["name"], conf["type"], conf["default"], conf["optional"], conf["description"]
                if name in LINKS:
                    name = BASE_LINK + LINKS[name] + f"[{name}]"
                type_ = " or ".join(type_) if isinstance(type_, list) else type_
                optional = "yes" if optional else "no"
                default = "null" if default is None else default
                
                line = f"| {name} | {type_} | {default} | {optional} | {description}"
                adoc_file.write(line + "\n")

            for note in algo.get("config_notes", []):
                adoc_file.write(f"5+| {note}\n")
