from jinja2 import Environment, FileSystemLoader
import os

HUBS = [
    {"hub_name": "Berlin_Alt_Tempelhof", "service_name": "berlin_alt_tempelhof", "container_name": "berlin-alt-tempelhof", "col_count": 19},
    {"hub_name": "Berlin_Danzinger", "service_name": "berlin_danzinger", "container_name": "berlin-danzinger", "col_count": 23},
    {"hub_name": "Berlin_Friedhof", "service_name": "berlin_friedhof", "container_name": "berlin-friedhof", "col_count": 19},
    {"hub_name": "Berlin_Hansaplatz", "service_name": "berlin_hansaplatz", "container_name": "berlin-hansaplatz", "col_count": 21},
    {"hub_name": "Berlin_Heinrich_Heine", "service_name": "berlin_heinrich_heine", "container_name": "berlin-heinrich-heine", "col_count": 35},
    {"hub_name": "Berlin_Hospital", "service_name": "berlin_hospital", "container_name": "berlin-hospital", "col_count": 39},
    {"hub_name": "Berlin_Humboldthain", "service_name": "berlin_humboldthain", "container_name": "berlin-humboldthain", "col_count": 32},
    {"hub_name": "Berlin_Innenstadt", "service_name": "berlin_innenstadt", "container_name": "berlin-innenstadt", "col_count": 11},
    {"hub_name": "Berlin_Linden", "service_name": "berlin_linden", "container_name": "berlin-linden", "col_count": 40},
    {"hub_name": "Berlin_Memhardstr", "service_name": "berlin_memhardstr", "container_name": "berlin-memhardstr", "col_count": 35},
    {"hub_name": "Berlin_Neukölln", "service_name": "berlin_neukolln", "container_name": "berlin-neukolln", "col_count": 17},
    {"hub_name": "Berlin_Potsdamer_Platz", "service_name": "berlin_potsdamer_platz", "container_name": "berlin-potsdamer-platz", "col_count": 37},
    {"hub_name": "Berlin_Prenzlauer_Berg", "service_name": "berlin_prenzlauer_berg", "container_name": "berlin-prenzlauer-berg", "col_count": 10},
    {"hub_name": "Berlin_Rehberge", "service_name": "berlin_rehberge", "container_name": "berlin-rehberge", "col_count": 14},
    {"hub_name": "Berlin_Remise", "service_name": "berlin_remise", "container_name": "berlin-remise", "col_count": 13},
    {"hub_name": "Berlin_Rosenthaler", "service_name": "berlin_rosenthaler", "container_name": "berlin-rosenthaler", "col_count": 37},
    {"hub_name": "Berlin_Savignyplatz", "service_name": "berlin_savignyplatz", "container_name": "berlin-savignyplatz", "col_count": 11},
    {"hub_name": "Berlin_Schöneberg", "service_name": "berlin_schoneberg", "container_name": "berlin-schoneberg", "col_count": 17},
    {"hub_name": "Berlin_Sophienkirchhof", "service_name": "berlin_sophienkirchhof", "container_name": "berlin-sophienkirchhof", "col_count": 23},
    {"hub_name": "Berlin_Spichernstr", "service_name": "berlin_spichernstr", "container_name": "berlin-spichernstr", "col_count": 13},
    {"hub_name": "Berlin_Tempelhof", "service_name": "berlin_tempelhof", "container_name": "berlin-tempelhof", "col_count": 29},
    {"hub_name": "Berlin_Wolff_Park", "service_name": "berlin_wolff_park", "container_name": "berlin-wolff-park", "col_count": 37},
    {"hub_name": "Berlin_hamburger", "service_name": "berlin_hamburger", "container_name": "berlin-hamburger", "col_count": 16},
]

os.makedirs("hub_zones", exist_ok=True)
os.makedirs("hub_zones/gateway_config", exist_ok=True)

env = Environment(loader=FileSystemLoader("."), trim_blocks=True, lstrip_blocks=True)
compose_tpl = env.get_template("template.yml.j2")
config_tpl  = env.get_template("gateway_config_template.yml.j2")

for hub in HUBS:
    # docker-compose
    compose_out = f"hub_zones/docker-compose.{hub['service_name']}.yml"
    with open(compose_out, "x") as f:
        f.write(compose_tpl.render(**hub))

    # gateway config
    config_out = f"hub_zones/gateway_config/{hub['service_name']}.yml"
    with open(config_out, "x") as f:
        f.write(config_tpl.render(**hub))

    print(f"{hub['hub_name']}: compose + config ({hub['col_count']} col)")