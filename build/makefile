JFLAGS = -g -Xlint:unchecked -d
JC = javac
SOURCE=../source/
TARGET=./color/

CLASSES = Color KdTree PaletteReader MostUsedColor SymbolReader PatternWriter ColorConv

default: $(addprefix $(TARGET), $(addsuffix .class, $(CLASSES)))

$(TARGET)%.class: $(SOURCE)%.java
	$(JC) $(JFLAGS) . $^

clean:
	$(RM) $(TARGET)*.class