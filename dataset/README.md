Python scripts:

- python3 get_dataset.py - create dataset.
- python3 dump_error_reps.py - dump errored repositories from Borges.
- python3 prepare_repo_list.py - Prepare list for Borges, taking only __slice__ off all items to save memory.

Scripts:

- cleanUp.sh - Restart Borges, download and archive new part of dataset using Borges and siva files.
- loop script - Loop Python scripts. Suitable for get_dataset.py (*PGA* and *Borges*).
- restart.sh - Restart Borges, PSQL, RabbitMQ and their docker images.
