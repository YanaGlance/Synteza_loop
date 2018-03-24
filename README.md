# Synteza_loop

Program pozwała rozwinąć semantycznie nieprawidłowe wyrażenie:

FOR(j, k, 15, >, -);

do poprawnej konstrukcji:

for (var j = k; j > 15; j--)
{
}

Do tego celu został użyty javaparser.
