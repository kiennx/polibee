package polibee.core.background;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Job Specs")
public class JobSpecs {

    private static boolean _tested = false;
    private static HashMap _hash = new HashMap();

    private AnnotationConfigApplicationContext _context;

    @Service
    class MockServiceCheckTestRunned {
        public void Test(HashMap params) {
            JobSpecs._tested = true;
        }
    }

    @Service
    class MockServiceCheckTestRunnedWithParams {
        public void Test(HashMap params) {
            JobSpecs._hash = params;
        }
    }

    @BeforeEach
    public void resetTested() {
        JobSpecs._tested = false;
        JobSpecs._hash = new HashMap();
    }

    @BeforeAll
    public void prepareBean() {
        _context = new AnnotationConfigApplicationContext(JobSpecs.class);
        _context.registerBean(MockServiceCheckTestRunned.class);
        _context.registerBean(MockServiceCheckTestRunnedWithParams.class);
    }

    @Test
    @DisplayName("Có thể thực thi một phương thức với kiểu params là HashMap theo beanName và method")
    public void shouldBeAbleToExecuteJob() throws Exception {
        Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunned\",\"method\":\"Test\",\"params\":\"\"}", _context);
        job.execute();

        assertEquals(true,JobSpecs._tested);
    }

    @Test
    @DisplayName("Có thể giữ nguyên tham số hashmap truyền vào")
    public void shouldBeAbleToRetainHashMap() throws JobException {
        Map<String, MockServiceCheckTestRunned> test = _context.getBeansOfType(MockServiceCheckTestRunned.class);

        Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithParams\",\"method\":\"Test\"," +
                "\"params\": {\"key\":\"retain\"}}", _context);
        job.execute();

        assertEquals(true,JobSpecs._hash.containsKey("key"));
        assertEquals("retain", JobSpecs._hash.get("key"));
    }
}
