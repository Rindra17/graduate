package hei.school.graduate.file.hash;

import hei.school.graduate.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
