#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};

#end
#parse("File Header.java")
import xbot.common.command.BaseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ${NAME} extends BaseSubsystem {
    @Inject
    public ${NAME}() {
    }

    @Override
    public void periodic() {
    }

    @Override
    public void refreshDataFrame() {
    }
}
