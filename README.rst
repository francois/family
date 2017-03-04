Family Manager
==============

This is the Family Manager, a suite of tools designed to work together and manage a family. This software was built when my children were 9 and 12, in the hopes of making them help with the family chores. One of my children also needed to keep a journal of what they eat, so I built this as well.


My Bank
-------

This software is designed to let children understand the concepts of banks, deposits, withdrawals, salary or revenue, interests and saving for future goals. My Bank expects each child to have his or her own account, configured by the parent. The parent will manage the list of chores, and will regularly post the child's salary. The system is designed to hand out at least one penny per day to children. Each family decides on the interest rate they want to pay out.

My Journal
----------

This is a food journal: at breakfast today, I ate an orange, a glass of milk and so on. The system does not have any databae of food, because it depends on what locality you are in, and how you want to manage it.


Deployment Target
-----------------

This software is a Scala program that depends on a PostgreSQL server for the heavy lifting. `Sqitch <http://sqitch.org/>`_ is used to manage database migrations. Look in `sqitch.conf` and `db/migrations` for the configuration and database migration files, respectively.


License
=======

Copyright 2017 François Beausoleil <francois@teksol.info>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
