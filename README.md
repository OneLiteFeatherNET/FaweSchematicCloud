# FAWESchematicCloud

## Description
FAWESchematicCloud is a simple and modern plugin to use the new schematic api from IntellectualSites

## Motivation
[FastAsyncWorldEdit](https://github.com/IntellectualSites/FastAsyncWorldEdit) still use the old schematic upload. 
This plugin is an addon to override the default download command from FastAsyncWorldEdit.
Also, that's handles the problem to don't bring breaking change into FastAsyncWorldEdit in

## Focus
Use the new [Arkitektonika](https://github.com/IntellectualSites/Arkitektonika) api to override the download command from fawe

## More information / external links

Hangar: Soon

Modrinth: Soon

Discord: https://discord.onelitefeather.de

## Permissions
- `worldedit.clipboard.download` - Allows the player  the FAWE download command

## Commands
- `/download` - Creates schematic download url

## Configuration
```yaml
arkitektonika:
  # The url of the backend server (Arkitektonika)
  backendUrl: https://api.schematic.cloud/
  # The url used to generate a download link from.
  downloadUrl: https://api.schematic.cloud/download/{key}
  # The url used to generate a deletion link from.
  deleteUrl: https://api.schematic.cloud/delete/{key}
web:
  # The url of the frontend
  frontend: https://schematic.cloud/
  # The url used to generate a download link from.
  downloadUrl: https://schematic.cloud/download/{key}
  # The url used to generate a deletion link from.
  deleteUrl: https://schematic.cloud/delete/{key}

```