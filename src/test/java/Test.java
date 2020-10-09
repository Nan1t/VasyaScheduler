import com.aspose.cells.ImageFormat;
import com.aspose.cells.Workbook;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class Test {

    @org.junit.Test
    public void test() throws Exception {
        Path path = Paths.get("C:\\Users\\Maxim\\Downloads\\4k.xls");
        Workbook workbook = new Workbook(path.toString());

        CompletableFuture<?> future = CompletableFuture.runAsync(()->{
            try{
                System.out.println("Generating...");

                BufferedImage image = new XlsToImage(workbook)
                        .format(ImageFormat.getPng())
                        .resolution(250)
                        .generate();

                ImageIO.write(image, "png", Paths.get("C:\\Users\\Maxim\\Downloads\\4k.png").toFile());

                workbook.dispose();
            } catch (Exception e){
                e.printStackTrace();
            }
        });

        future.get();
    }
}