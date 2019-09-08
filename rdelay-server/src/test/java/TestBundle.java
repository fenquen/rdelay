import com.alibaba.fastjson.JSON;
import org.junit.Test;

public class TestBundle {
    @Test
    public void testEnum() {
        String a = "{\"season\":\"SPRING\"}";
        Description description = JSON.parseObject(a,Description.class);
        System.out.println(description.season.name);

    }

    static class Description {
        public Season season;
    }

    public enum Season {

        SPRING("spring");

        public final String name;

        Season(String name) {
            this.name = name;
        }
    }
}
