package domain
import utest._

import scala.concurrent.Future

/**
 *
 */
object FrameworkAsyncTests extends TestSuite{
  
  implicit val ec = utest.ExecutionContext.RunNow
  def tests = TestSuite{
    'asyncFailures {
      Future {
        assert(true)
      }
    }
  }
}