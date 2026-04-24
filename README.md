# About program
Небольшая утилита позволяющая grab/dump/take/get/~steal~ байт код из работующей виртуальной машины Java и сохранять его на диск.
Имеет графический интерфейс, несколько форматов сохранения, совместима с jdk 1.8+, и небольшой пред-загрузцик позволяющий выбирать java для запуска
## Usage

> [!NOTE]
> Из-за специфики приложения, для запуска нужна JDK (не JRE)

Если вы запустите приграмму на JRE то откроется окно пред-загрузщика  
и вам будет предложено выбрать JDK для запуска  
> ![Bootstrap](https://github.com/Artur114Projects/JVMByteCodeGrabber/blob/master/images/bcg-bootstrap.png)

Введите туда путь до jdk, например: `C:\Program Files\Java\jdk1.8.0_231`  
Кнопка launch перезапустит приложение на указанной jdk  
Если запуск не удастся вы увидете окно пред-загрузщика сново

### Main frame

После запуска вы увидите список запущеных виртуальных машин. Выберете нужную JVM, подключитесь, добавте нужниые вам классы во вторую панель с помощью кнопки add to grab (самая правая кнопка левой панели)

![Screen 1](https://github.com/Artur114Projects/JVMByteCodeGrabber/blob/master/images/bcg-screen-1.png)  
![Screen 2](https://github.com/Artur114Projects/JVMByteCodeGrabber/blob/master/images/bcg-screen-2.png)  
![Screen 3](https://github.com/Artur114Projects/JVMByteCodeGrabber/blob/master/images/bcg-screen-3.png)  

### Class grabbing

Чтобы сохранить выбранные классы нажмине кнопку grab (нижняя правая кнопка)
Если путя из текстового поля с права не существует откроется File chooser, выбирете файл формата `.zip`, `.jar`, или папку

> [!NOTE]
> При выборе файла не поддерживаемого формата его формат будет автомотически заменен на `.jar`

После вам будет предложенно выбрать формат записи

> ![Screen 4](https://github.com/Artur114Projects/JVMByteCodeGrabber/blob/master/images/bcg-screen-4.png)

Есть 3 формата записи

- Full package
- Package + Class name
- Just class name

**Full package:**  
- Сохраняет каждый класс так что его package является путем в файловой системе, например: класс `java.util.List` будет в файловой системе `java\util\List.class`

**Package + Class name**
- Сохраняет каждый класс так что его именем будет package + classname, например: класс `java.util.List` будет в файловой системе `java.util.List.class`

**Just class name**
- Сохраняет каждый класс так что его именем будет classname при условии что файлов с таким именем нет иначе package + classname, например: класс `java.util.List` будет в файловой системе `List.class`

## Credits
