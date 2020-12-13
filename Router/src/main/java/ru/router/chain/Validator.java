package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.model.Fix;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Data
@Slf4j
public class Validator implements Chain {

    private Chain next = null;

    @Override
    synchronized public void handle(Fix message) {
        if (validate(message.getInput(), message.getCheckSum())) {
            next.handle(message);
        }
    }

    private boolean validate(String message, String checkSum) {
        byte bytes[] = message.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        long checksumValue = checksum.getValue();

        String expectedCheckSum = String.valueOf(checksumValue);
        log.info("Income check sum: '{}'\texpected check sum: '{}' equal? {}", checkSum, expectedCheckSum, expectedCheckSum.equals(checkSum));
        if (expectedCheckSum.equals(checkSum))
            return true;
        log.info("Invalid checkSum");
        return false;
    }
}
