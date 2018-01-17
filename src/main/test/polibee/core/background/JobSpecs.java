package polibee.core.background;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import polibee.core.PolibeeException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Service
    class MockServiceCheckTestRunnedWithArrayParams {
        public void Test(String test, Double d1, Double d2, Boolean True)
        {
            JobSpecs._tested = true;
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
        _context.registerBean(MockServiceCheckTestRunnedWithArrayParams.class);
    }

    @Test
    @DisplayName("Có thể thực thi một phương thức với kiểu params là HashMap theo beanName và method")
    public void shouldBeAbleToExecuteJob() throws Exception {
        Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunned\",\"method\":\"Test\",\"params\":\"\"}", _context);
        job.execute();

        assertEquals(true,JobSpecs._tested);
    }

    @Test
    @DisplayName("Có thể thực thi một phương thức với kiểu params là HashMap bên trong chứa array tham số theo beanName và method")
    public void shouldBeAbleToExecuteJobWithArrayParams() throws JobException {
        Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithArrayParams\",\"method\":\"Test\"," +
                "\"params\":{\"params\": [\"test\", 1, 2, true]}}", _context);
        job.execute();

        assertEquals(true,JobSpecs._tested);
    }

    @Test
    @DisplayName("Có thể giữ nguyên tham số hashmap truyền vào")
    public void shouldBeAbleToRetainHashMap() throws JobException {
        Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithParams\",\"method\":\"Test\"," +
                "\"params\": {\"key\":\"retain\"}}", _context);
        job.execute();

        assertEquals(true,JobSpecs._hash.containsKey("key"));
        assertEquals("retain", JobSpecs._hash.get("key"));
    }

    @Test
    @DisplayName("Sẽ ném exception khi json data không đúng chuẩn")
    public void shouldThrowExceptionWhenJsonDataFails() {
        Executable closureContainingCodeToTest = () -> {
            Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithParams\",\"method\":\"Test\"," +
                    "\"params\": {\"key\":\"retain\"}", _context);
            job.execute();
        };
        assertThrows(JobException.class, closureContainingCodeToTest);
    }

    @Test
    @DisplayName("Sẽ ném exception khi không tìm thấy service bean")
    public void shouldThrowExceptionWhenServiceNotFound() {
        Executable closureContainingCodeToTest = () -> {
            Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithNoClass\",\"method\":\"Test\"," +
                    "\"params\": {\"key\":\"retain\"}}", _context);
            job.execute();
        };
        assertThrows(JobException.class, closureContainingCodeToTest);
    }

    @Test
    @DisplayName("Sẽ ném exception khi không tìm thấy method")
    public void shouldThrowExceptionWhenMethodNotFound() {
        Executable closureContainingCodeToTest = () -> {
            Job job = new Job("{\"service\":\"jobSpecs.MockServiceCheckTestRunnedWithParams\",\"method\":\"NoSuchMethod\"," +
                    "\"params\": {\"key\":\"retain\"}}", _context);
            job.execute();
        };
        assertThrows(JobException.class, closureContainingCodeToTest);
    }
}
