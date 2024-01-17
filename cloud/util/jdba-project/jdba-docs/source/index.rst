.. JDB-Admin documentation master file, created by
.. sphinx-quickstart on Sat Jan 13 16:02:02 2024.
.. You can adapt this file completely to your liking, but it should at least
.. contain the root `toctree` directive.

Welcome to JDB-Admin's documentation!
=====================================

.. toctree::
   :maxdepth: 1
   :caption: Contents:

    Quick Start <quick-start>
    Architecture <architecture>
    Road Map <road-map>


name
----

JDBA is a riff on PhpMyAdmin, which is a tool for administration of MySQL databases.
JDBA would be called JdbcMyAdmin, except that this tool is not specifically designed for
MySQL databases, in fact it aims to achieve feature parity with all supported databases.
Though many database drivers for java implement the
**J**\ ava **D**\ ata\ **B**\ ase **C**\ onnectivity API contract,
the primary purpose of JDBA is not necessarily to expose that level of functionality,
but rather to give people a user interface for common database querying and administrative
tasks.


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
