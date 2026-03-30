# Coupon Service

Backendowy serwis REST do tworzenia i realizacji kuponów rabatowych z ograniczeniem kraju oraz limitem użyć.

## Zakres rozwiązania

Projekt implementuje dwa główne przypadki użycia:
- tworzenie nowego kuponu,
- rejestrację wykorzystania kuponu przez użytkownika.

W obecnej wersji serwis:
- traktuje kody kuponów case-insensitive przez normalizację do uppercase,
- ogranicza użycie kuponu do wskazanego kraju na podstawie GeoIP,
- pilnuje limitu użyć kuponu,
- pozwala wykorzystać dany kupon tylko raz na użytkownika,
- zapisuje dane w PostgreSQL,
- zawiera testy jednostkowe, testy warstwy webowej (`MockMvc`) oraz testy integracyjne.

## Decyzje techniczne

- Java 17 i Spring Boot 3
- PostgreSQL jako docelowa baza danych
- Flyway do wersjonowania schematu
- Spring Data JPA do persystencji
- Testcontainers do testów integracyjnych z PostgreSQL

## Kilka świadomych decyzji

- Kod kuponu jest normalizowany na wejściu, żeby uniknąć niejednoznaczności typu `WIOSNA` vs `wiosna`.
- Dla realizacji kuponu użyta jest blokada pesymistyczna na rekordzie kuponu, żeby ograniczyć problemy przy równoległych żądaniach.
- Dodatkowo unikalność pary `(coupon_id, user_id)` jest zabezpieczona w bazie danych, więc baza pozostaje ostatecznym mechanizmem ochrony przed duplikatem.
- Integracja GeoIP jest schowana za prostym portem, żeby dało się ją zamockować w testach i łatwo podmienić dostawcę.
- Nie rozdzielałem rozwiązania na mikroserwisy, ponieważ domena jest mała, a kluczowy proces realizacji kuponu korzysta ze spójnej, prostej transakcji obejmującej walidację kuponu i zapis użycia. W tym przypadku modularny monolit daje mniejszą złożoność operacyjną i prostsze utrzymanie niż komunikacja między usługami.

## Jak podzieliłbym to na mikroserwisy

Gdyby domena lub skala systemu istotnie urosły, rozważyłbym następujący podział:

- `coupon-service` jako właściciel definicji kuponów: tworzenie kuponów, reguły ważności, limit użyć, ograniczenie do kraju.
- `redemption-service` jako właściciel historii użyć: rejestracja realizacji kuponów, pilnowanie zasady "jeden użytkownik może użyć kuponu tylko raz", raportowanie wykorzystań.
- `geoip-service` albo wydzielony komponent infrastrukturalny: mapowanie IP na kraj, cache wyników, retry i izolacja zewnętrznego dostawcy.

Taki podział byłby naturalny, bo oddzielałby definicję kuponu, proces realizacji i integrację z usługą zewnętrzną. Najwcześniej wydzieliłbym GeoIP, ponieważ to najsłabiej powiązana część domeny i jednocześnie jedyne miejsce zależne od zewnętrznego API. Rozdzielenie kuponów i realizacji wprowadzałoby już dodatkową złożoność związaną z utrzymaniem spójności między usługami, dlatego w obecnym zakresie uznałem to za niepotrzebne.

## Ograniczenia

- Integracja GeoIP korzysta z zewnętrznej usługi `ipwho.is`, więc w środowisku produkcyjnym warto dodać retry, monitoring i ewentualny fallback.
- Testy integracyjne wymagają działającego Dockera.
- Projekt jest mały, więc architektura jest nieco bardziej uporządkowana niż byłoby to konieczne dla prostego CRUD-a, ale pozwala czytelnie oddzielić domenę od infrastruktury.

## Treść zadania

Zadanie rekrutacyjne backend

Celem zadania jest zaprojektowanie i zaimplementowanie REST-owego serwisu odpowiedzialnego za
zarządzanie kuponami rabatowymi. System powinien udostępniać następujące funkcjonalności:
• rejestrację użycia kuponu przez użytkownika,
• tworzenie nowego kuponu (obsługa uwierzytelniania nie jest wymagana).
Każdy kupon powinien zawierać następujące informacje:
• unikalny kod kuponu,
• datę utworzenia,
• maksymalną liczbę możliwych użyć,
• bieżącą liczbę użyć,
• kraj, dla którego kupon jest przeznaczony.
Wymagania biznesowe:
• Kod kuponu powinien być unikalny, wielkość znaków nie ma znaczenia (WIOSNA i wiosna
traktujemy jak ten sam).
• Wykorzystanie kuponu powinno być limitowane maksymalną liczbą użyć - kto pierwszy ten
lepszy.
• Kraj zdefiniowany w kuponie ogranicza użycie kuponu tylko do osób z danego kraju (na
podstawie adresu IP - można wykorzystać dowolną darmową usługę do tego).
• Gdy kupon osiągnął maksymalną liczbę zużyć, próby użycia go powinny zwracać stosowną
informację w zwrotce. Tak samo, gdy podany kod kuponu nie istnieje, próba zużycia
przychodzi z niedozwolonego kraju lub użytkownik zużył już dany kupon.
• (Opcjonalnie, dla chętnych) Jeden użytkownik może wykorzystać kupon tylko raz – request
powinien zawierać identyfikator użytkownika (dowolny) oraz kod kuponu do wykorzystania.

Rozwiązanie powinno byś skalowalne. Dane powinny być zapisywane w bazie danych. Serwis
powinien być zaimplementowany w Java lub Kotlin. Projekt powinien być możliwy do zbudowania za
pomocą Maven lub Gradle. Możesz wspierać się dowolnymi,darmowymi, łatwo dostępnymi
technologiami (silniki BD, biblioteki, frameworki).
Przygotowując rozwiązanie zadania, pamiętaj, że zależy nam nie tylko na działającym kodzie, ale
również na jego jakości i podejściu do projektowania. Oczekujemy, że zaprezentujesz styl pracy jak
najbardziej zbliżony do tego, jakbyś realizował zadanie w realnym projekcie produkcyjnym.
Zachęcamy do stosowania dobrych praktyk programistycznych, przemyślanej architektury,
odpowiednich wzorców projektowych oraz rozwiązań technologicznych, które pokazują Twoje

zrozumienie tworzenia oprogramowania gotowego do wdrożenia. Unikaj uproszczonych
implementacji tworzonych jedynie na potrzeby spełnienia minimalnych wymagań zadania.
Cenimy przejrzystość, czytelność i jakość kodu, a także uwzględnienie kontekstu, że dany system
mógłby funkcjonować w wielowątkowym środowisku produkcyjnym.
Zależy nam na tym, aby kod był tworzony samodzielnie, bez wspomagania sztuczną inteligencją.
Prosimy o umieszczenie projektu na dowolnym repozytorium i udostępnienie nam linku.

## Uruchomienie lokalne

### Wymagania
- Java 17
- Docker (uruchomiony Docker Desktop)

### One-liner: uruchom PostgreSQL dla aplikacji
```bash
docker run --name coupon-postgres -e POSTGRES_DB=coupon_service -e POSTGRES_USER=coupon -e POSTGRES_PASSWORD=coupon -p 5432:5432 -d postgres:16-alpine
```

### Start aplikacji
```bash
./mvnw spring-boot:run
```

Na Windows (PowerShell):
```powershell
.\mvnw.cmd spring-boot:run
```

## Testy

```bash
./mvnw test
```

Na Windows (PowerShell):
```powershell
.\mvnw.cmd test
```

## Przykladowe requesty

Ponizej sa dwa przykladowe requesty, ktore pozwalaja szybko sprawdzic dzialanie API z poziomu Postmana lub po eksporcie do `cURL`.

### Utworzenie kuponu

```bash
curl --location 'http://localhost:8080/api/coupons' \
--header 'Content-Type: application/json' \
--data '{
  "code": "WIOSNA2026",
  "maxRedemptions": 10,
  "countryCode": "PL"
}'
```

### Realizacja kuponu

```bash
curl --location 'http://localhost:8080/api/coupons/WIOSNA2026/redemptions' \
--header 'Content-Type: application/json' \
--header 'X-Forwarded-For: 83.24.12.10' \
--data '{
  "userId": "user-123"
}'
```

W lokalnym uruchomieniu naglowek `X-Forwarded-For` jest uwzgledniany, poniewaz aplikacja traktuje adresy loopback (`127.0.0.1`, `::1`) jako zaufane proxy. W środowisku innym niż lokalne analogicznie nalezy skonfigurowac adresy zaufanych proxy przez `security.trusted-proxies` lub zmienna srodowiskowa `TRUSTED_PROXIES`.
