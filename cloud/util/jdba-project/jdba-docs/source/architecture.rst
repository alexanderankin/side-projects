Architecture
============

JDBAdmin is structured as a SPA application which is embedded in a Spring Boot backend.

.. contents::
   :depth: 2
   :local:

frontend
--------

The frontend is a react application which is built with vite.
It is configurable based on a base url.
Theoretically, in the future, you can try ``/api/version`` on various parent levels
to find the base path of the application, but its unlikely to be recommended.
Instead, configure the application with the base url.

The frontend is not intended to be backwards compatible with php my admin.

security
------

You can configure the backend to store the database credentials in the configuration,
or you can input the database credentials upon login to the application.

There are no users as such in ``jdba``.
