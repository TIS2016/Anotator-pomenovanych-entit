Jednoduch� prototyp na testovanie.

1. Spustite "Server Prototype.py"

2. Spustite "Test Client.py"

3. Server zatia� dok�e handlova� n�sleduj�ce requesty:

- "login|||<username>|||<password>"
- "loginlist"

Pozor, case-sensitive, ak je chyba v requeste, server ho posle nazad.
Login funguje podla suboru users.txt v priecinku DB.

Kni�nica table.py je mnou naprogramovan� datab�za. T� by u� mala by� vo fin�lnej podobe,
tak�e ak sa tam bud� robi� nejak� zmeny, tak bud� iba minim�lne.