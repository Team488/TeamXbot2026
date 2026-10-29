package competition.electrical_contract;

import javax.inject.Inject;
import java.util.EnumSet;

public class UnitTestCompetitionContract extends Contract2026 {

    @Inject
    public UnitTestCompetitionContract() {
        super(EnumSet.allOf(Hardware.class));
    }
}
