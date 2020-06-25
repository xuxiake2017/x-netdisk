package group.xuxiake.web.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.spring.JodConverterBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author by xuxiake, Date on 2020/5/20 17:54.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
@Component
public class DocConverterUtil {

    @Resource
    private JodConverterBean jodConverterBean;

    public byte[] docToPDF(InputStream inputStream, String extName) {

        if (inputStream == null || StringUtils.isAnyEmpty(extName)) {
            return null;
        }
        DocumentConverter converter = jodConverterBean.getConverter();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            extName = extName.toUpperCase();
            DocumentFormat documentFormat = null;
            switch (extName) {
                case "DOCX":
                    documentFormat = DefaultDocumentFormatRegistry.DOCX;
                    break;
                case "DOC":
                    documentFormat = DefaultDocumentFormatRegistry.DOC;
                    break;
                case "XLS":
                    documentFormat = DefaultDocumentFormatRegistry.XLS;
                    break;
                case "XLSX":
                    documentFormat = DefaultDocumentFormatRegistry.XLSX;
                    break;
                case "PPT":
                    documentFormat = DefaultDocumentFormatRegistry.PPT;
                    break;
                case "PPTX":
                    documentFormat = DefaultDocumentFormatRegistry.PPTX;
                    break;
                case "TXT":
                    documentFormat = DefaultDocumentFormatRegistry.TXT;
            }
            converter.convert(inputStream)
                    .as(documentFormat)
                    .to(byteArrayOutputStream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();
            return byteArrayOutputStream.toByteArray();
        } catch (OfficeException | IOException e) {
            log.error("convert pdf error");
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public byte[] pdfToDoc(InputStream inputStream, String extName) {

        if (inputStream == null || StringUtils.isAnyEmpty(extName)) {
            return null;
        }
        DocumentConverter converter = jodConverterBean.getConverter();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            converter.convert(inputStream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .to(byteArrayOutputStream)
                    .as(DefaultDocumentFormatRegistry.DOCX)
                    .execute();
            return byteArrayOutputStream.toByteArray();
        } catch (OfficeException | IOException e) {
            log.error("convert pdf error");
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
