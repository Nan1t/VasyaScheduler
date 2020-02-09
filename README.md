# Планировщик Вася

#### Содержание

1) [Поддерживаемые мессенджеры](#messengers)
2) [Пример работы с ботом](#example)
3) [Формат составленного расписания](#format)
4) [Команды](#commands)

Планировщик Вася поможет Вам с нелегким процессом проверки расписания пар.
После того как Вы добавите себя в базу данных бота, в Ваши личные сообщения будет регулярно присылаться свежее расписание на текущую неделю, любезно составленное ботом лично для Вашего аккаунта.

## <a name="messengers">Поддерживаемые мессенджеры</a>

Бот может существовать одновременно на множестве мессенджеров. 
При этом работа с ботом почти ничем не будет различаться, 
за исключением начального создания диалога с ним.

На данный момент бот поддерживает следующие мессенджеры:
- Telegram `@VasyaSchedulerBot`
- Facebook messenger (Скоро)

## <a name="example">Пример работы с ботом</a>

Для примера мы будем использовать Telegram бота.

**Переписать пример работы по новой схеме**

##### Составленное расписание будет выглядеть примерно так:
![alt text](examples/schedule_example.png "Пример составленного расписания")

## <a name="format">Формат составленного расписания</a>

Составленное ботом расписание конверитруется в изображение 
формата PNG, для удобства просмотра на мобильных устройствах.

Внешне оно похоже на расписания для студентов. Каждая пара делится на 4 строки:

На **1** строке находится название предмета.  
На **2** строке - тип пары указанный в расписании у студентов. Если тип отсутствует в составленном расписании, значит он отсутствует и в расписании студентов.  
На **3** или последлующих строках - список групп, которые должны присутствовать на паре  
На **последней** строке - номер аудитории

## <a name="commands">Команды</a>

Команды, описанные ниже, работают на всех поддерживаемых мессенджерах.

##### Помощь  
`/help` или `/h`

##### Подписаться на рассылку расписания преподавателя  
`/teacher <Ф> <И> <О>`  
или  
`/t <Ф> <И> <О>`

Имя и инициалы следует указывать в том же виде и на том же языке, на котором они указаны в таблице с расписанием.
В противном случае, программа не сможет распознать вас в таблицах.

Например:  
`/teacher Пупкiн В А`  
или  
`/t Шишкiн С П`

После первого ввода этой команды, вы можете просто вводить `/t`, игнорируя ФИО, для получения свежего распиания.
Если вы ввели ФИО, но уже были подписаны на какое-то расписание, вы просто подпишитесь на другое.

##### Отписаться от рассылки расписания преподавателя
`/teacher deny` или `/t deny`

##### Консультации  
`/c` - Показать личных дни консультации  
`/c all` - Посмотреть консультации всех преподавателей

Для получения личных дней консультаций, эту команду следует вводить после подписки на расписание преподавателя.

##### Расписание студентов
`/s list` - Вывести номера (id) всех расписаний  
`/s <id>` - Подписаться на рассылку расписания студентов под номером `<id>`
`/s` - Посмотреть расписание студентов, на которое вы подписаны

##### Оценки студентов
`/p <Ф> <И> <О> <пароль>` - Посмотреть свои баллы по предметам. ФИО и пароль вводятся так же как на сайте ZIEIT.  
`/p` - Посмотреть свои баллы по предметам, на основании введенных ранее данных.

После первого воода ФИО и пароля бот запоминает ваши данные, 
и в слудующий раз в ыможете вводить только команду `/p`.




`/subscribe_teacher` - Подписаться на расписание преподавателя  
`/teacher` - Посмотреть расписание преподавателя, на которого вы подписаны  
`/subscribe_students` - Подписаться на расписание студентов
`/student` - Посмотреть расписание студентов, на которое вы подписаны




> **Важно!** В целях безопасности оценки стоит смореть в личных сообщениях с ботом, 
а не в публичных группах.