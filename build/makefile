JFLAGS = -g -d
JC = javac
SOURCE=../source/
TARGET=./color/

CLASSES = ColorConv DMC Color KdTree LAB PaletteReader

default: $(addprefix $(TARGET), $(addsuffix .class, $(CLASSES)))

$(TARGET)%.class: $(SOURCE)%.java
	$(JC) $(JFLAGS) . $^

clean:
	$(RM) $(TARGET)*.class