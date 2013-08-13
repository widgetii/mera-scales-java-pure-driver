Драйвер и приложение для тестирования весов "Мера"
==================================================

Сборка
------

В корне запустить команду
    ant

JAR файлы будут созданы в директории `target`.


Структура проекта
-----------------

Проект состоит из нескольких модулей (подпроектов), каждый в своей директории:

- `pjcomm` - доработанный и исправленный [PureJavaComm][]. Изменения:
    - Исправлен баг [#38](https://github.com/nyholku/purejavacomm/issues/38).
    - API дополнено возможностью передачи настроек по умолчанию при открытии
      порта.
    - В API добавлены классы, объединяющие настройки, обычно передаваемые как
      отдельные параметры методов, но требующиеся в любом приложении.
      Используются в том числе и в качестве параметров по умолчанию.
    - Исправление множества предупреждений компилятора.
    - Множественный рефакторинг с целью приведения кода к виду, более привычному
      для Java программистов. PureJavaComm написан Си-программистами в
      характерном для них стиле, а потому непонятно: кто и как будет развивать
      PureJavaComm. Точно не Java-программисты, которых отучают писать такой
      код.
- `driver` - асинхронный драйвер для работы с весами. Зависит от `pjcomm`.
  Содержит:
    - клиентское API,
    - API драйверов,
    - реализацию драйверов для протокола [Byte9][],
    - тесты.
- `tester` - тестовое приложение. Зависит от `driver`.

[PureJavaComm]: https://github.com/nyholku/purejavacomm
[Byte9]:        http://www.mera-device.ru/scales.pdf

Модули можно собирать все вместе или по отдельности, при условии что
зависимости модуля уже собраны.


Тестовое приложение
-------------------

### Запуск ###

После сборки приложение для тестирования можно запустить командой
    java -jar target/ru.aplix.mera.tester.jar


### Порядок работы ###

Из выпадающего списка выберите порт, к которому подключено устройство (весы)
и протокол работы с ним. Например, `COM1 (Byte9)` означает устройство,
подключённое к порту `COM1` и работающее по протоколу `Byte9`.

После выбора порта осуществляется подключение к устройству. Статус подключения
можно видеть в строке состояния внизу.

Чтобы начать взвешивание, нажмите кнопку `Взвешивать`. Чтобы остановить
взвешивание - нажмите её же (надпись на кнопке во время взвешивания изменится на
`Остановить`). При выборе другого устройства взвешивание останавливается
автоматически. Состояние и результат взвешивания отображается справа от кнопки.

Все действия и результаты работы также отображаются в основном окне приложения
в виде текстовых сообщений.


### Настройки ###

Настройки влияют на работу драйверов и могут привести к неработоспособности.

В приложении предусмотрены разные типы настроек:

- Общие настройки влияют на все устройства.
- Настройки `Byte9` - только на устройства, работающие по этому протоколу.

Если значение настройки не указано, то используется значение по умолчанию,
заданное драйвером. В противном случае используется указанное значение.
У некоторых настроек есть выпадающий список возможных, но не обязательно
рабочих значений. Значения также можно указывать вручную.

Поскольку нет способа определить автоматически, подключено ли устройство к
выбранному порту, драйвер периодически пытается подключиться к нему. В случае
успеха попытки прекращаются. Иначе драйвер пытается подключиться снова и снова,
постепенно увеличивая временной интервал между попытками. Границы этого
интервала в миллисекундах как раз и задаются настройками
"Наименьшая задержка переподключения" и "Наибольшая задержка переподключения".

Значение настройки "Период взвешивания" задаёт время в миллисекундах между
взвешиваниями. При изменении веса состояние меняется на "Загружено" или
"Разгружено". Если три взвешивания подряд показывают одинаковый вес, то этот вес
считается окончательным (стабильным). Этот вес и отображается.

Настройки `Byte9`:

- "Время ожидания подключения" - сколько времени ждать, пока порт не
  освободится. Имеет значение, если порт используется другим приложением.
- "Повторных посылок команды". Команда, посланная весам, не всегда ими
  выполняется с первого раза. Иногда приходится посылать команду повторно.
  Данная настройка указывает, сколько раз можно пытаться это сделать.
  0 означает, что команда не будет послана во второй раз в случае ошибки.
  В случае, если команду послать так и не удалось, соединение с устройством
  считается разорванным, все операции с ним (например, взвешивание)
  прекращаются, а драйвер начинает попытки переустановить соединение.
- "Время ожидания ответа" задаёт время в миллисекундах, которое драйвер будет
  ожидать ответа от устройства. Если в течение отведённого времени ответ так и
  не придёт, драйвер либо попытается послать команду повторно, либо выдаст
  ошибку соединения, в зависимости от того, сколько повторных попыток отправки
  команды разрешено сделать предыдущей настройкой.
- "Задержка отправки данных" - время в миллисекундах между отправкой адреса и
  тела пакета, содержащего команду (см. [Byte9][]). Какая-то задержка
  необходима, поскольку без неё весы не отвечают.
