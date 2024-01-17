Quick Start
===========

This page will contain information about using the software.

For now, all such information will be stored here.

Configuration
-------------

Here are the configuration options which are available:


.. class::  abc def

    ``Type``: string


.. config:option:: $cfg['PmaAbsoluteUri']

    :type: string
    :default: ``''``

    .. versionchanged:: 4.6.5

        This setting was not available in phpMyAdmin 4.6.0 - 4.6.4.

    Sets here the complete :term:`URL` (with full path) to your phpMyAdmin
    installation's directory. E.g.
    ``https://www.example.net/path_to_your_phpMyAdmin_directory/``. Note also
    that the :term:`URL` on most of web servers are case sensitive (even on
    Windows). Don’t forget the trailing slash at the end.

    Starting with version 2.3.0, it is advisable to try leaving this blank. In
    most cases phpMyAdmin automatically detects the proper setting. Users of
    port forwarding or complex reverse proxy setup might need to set this.

    A good test is to browse a table, edit a row and save it. There should be
    an error message if phpMyAdmin is having trouble auto–detecting the correct
    value. If you get an error that this must be set or if the autodetect code
    fails to detect your path, please post a bug report on our bug tracker so
    we can improve the code.

    .. seealso:: :ref:`Architecture`, :ref:`faq2_5`, :ref:`faq4_7`, :ref:`faq5_16`
