package org.springframework.core.type.classreading;

import org.springframework.asm.ClassReader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

public class MultipleAnnotationValuesMetadataReader implements MetadataReader {
	
	private final ClassReader classReader;

	private final ClassLoader classLoader;

	public MultipleAnnotationValuesMetadataReader(ClassReader classReader,
			ClassLoader classLoader) {
		this.classReader = classReader;
		this.classLoader = classLoader;
	}
	
	public ClassMetadata getClassMetadata() {
		ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
		this.classReader.accept(visitor, true);
		return visitor;
	}
	
	public AnnotationMetadata getAnnotationMetadata() {
		MultipleValuesAnnotationMetadataReadingVisitor visitor = new MultipleValuesAnnotationMetadataReadingVisitor(this.classLoader);
		this.classReader.accept(visitor, true);
		return visitor;
	}

}
