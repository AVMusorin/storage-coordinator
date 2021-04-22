### storage-coordinator

### Описание
Оркестрация ETL процессов с помощью FSM (finite state machine). 
Хранилище может находиться в определенный момент времени в каком-то состоянии ([FSMStateType](modules/core/src/main/scala/domain/states.scala))
Для перехода между состояниями используются события ([EventType](modules/core/src/main/scala/domain/states.scala))
Переход между событиями должен быть описан своим обработчиков ([ActionHandler](modules/core/src/main/scala/algebras/handler.scala))

### modules

- [algebras](modules/core/src/main/scala/algebras) - описание алгебры
- [config](modules/core/src/main/scala/config) - работа с конфигурацией
- [domain](modules/core/src/main/scala/domain) - описание доменов
- [http](modules/core/src/main/scala/http) - работа с http, в том числе с routes
- [modules](modules/core/src/main/scala/modules) - абстракция над модулями системы

### Examples
#### Получение текущего состояния
`GET http://localhost:8080/v1/fsm`

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Thu, 22 Apr 2021 12:10:31 GMT
Content-Length: 16

{
  "state": "Init"
}
```

#### Отправка события
```
POST http://localhost:8080/v1/fsm
Content-Type: application/json
Accept: application/json

{ "event" : "Start" }
```

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Thu, 22 Apr 2021 12:11:27 GMT
Content-Length: 16

{
  "state": "Load"
}
```
Если обработчик успешно выполнил все действия - возвращает новое состояние системы