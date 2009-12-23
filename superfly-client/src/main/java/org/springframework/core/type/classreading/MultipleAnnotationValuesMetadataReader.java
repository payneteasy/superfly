package org.springframework.core.type.classreading;

import org.springframework.asm.ClassReader;
import org.springframework.core.type.AnnotationMetadata;

public class MultipleAnnotationValuesMetadataReader extends
		SimpleMetadataReader {
	
	private final ClassReader classReader;

	private final ClassLoader classLoader;

	public MultipleAnnotationValuesMetadataReader(ClassReader classReader,
			ClassLoader classLoader) {
		super(classReader, classLoader);
		this.classReader = classReader;
		this.classLoader = classLoader;
	}
	
	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		MultipleValuesAnnotationMetadataReadingVisitor visitor = new MultipleValuesAnnotationMetadataReadingVisitor(this.classLoader);
		this.classReader.accept(visitor, true);
		return visitor;
	}

}
