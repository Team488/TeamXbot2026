#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};

#end
#parse("File Header.java")
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ${NAME} extends BaseCommand {
    @Inject
    public ${NAME}() {
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void execute() {
    }
}
