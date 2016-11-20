Jednoduchı prototyp na testovanie.

1. Spustite "Server Prototype.py"

2. Spustite "Test Client.py"

3. Server zatia¾ dokáe handlova následujúce requesty:

- "login|||<username>|||<password>"
- "loginlist"

Pozor, case-sensitive, ak je chyba v requeste, server ho posle nazad.
Login funguje podla suboru users.txt v priecinku DB.

Kninica table.py je mnou naprogramovaná databáza. Tá by u mala by vo finálnej podobe,
take ak sa tam budú robi nejaké zmeny, tak budú iba minimálne.