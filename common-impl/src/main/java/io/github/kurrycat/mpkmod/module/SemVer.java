package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.IVersion;
import io.github.kurrycat.mpkmod.api.module.IVersionConstraint;
import io.github.kurrycat.mpkmod.api.module.InvalidVersionConstraintException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public record SemVer(int major, int minor, int patch) implements IVersion {
    private static final Pattern VERSION_REGEX = Pattern.compile("\\d+(\\.\\d+){0,2}");

    public static SemVer parse(String version) throws InvalidVersionFormatException {
        if (version == null || !VERSION_REGEX.matcher(version).matches()) {
            throw new InvalidVersionFormatException("Invalid version format: '" + version + "'");
        }

        String[] parts = version.trim().split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return new SemVer(major, minor, patch);
    }

    @Override
    public @NotNull String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean satisfies(IVersionConstraint range) {
        return range.isSatisfiedBy(this);
    }

    @Override
    public int compareTo(@NotNull IVersion o) {
        if (!(o instanceof SemVer(int major1, int minor1, int patch1))) {
            throw new IllegalArgumentException("Cannot compare with non-SemVer implementation");
        }
        if (this.major != major1) return Integer.compare(this.major, major1);
        if (this.minor != minor1) return Integer.compare(this.minor, minor1);
        return Integer.compare(this.patch, patch1);
    }

    public static class InvalidVersionFormatException extends Exception {
        public InvalidVersionFormatException(String message) {
            super(message);
        }
    }

    public record Constraint(
            SemVer version,
            Operator operator
    ) implements IVersionConstraint {
        public Constraint {
            if (version == null) throw new IllegalArgumentException("Version cannot be null");
            if (operator == null) throw new IllegalArgumentException("Operator cannot be null");
        }

        @Override
        public boolean isSatisfiedBy(IVersion version) {
            return operator.apply(this.version, version);
        }

        @Override
        public @NotNull String toString() {
            return operator.symbol() + version.toString();
        }

        public enum Operator implements IVersionOperator {
            GREATER_THAN_OR_EQUAL(">=", (self, other) -> self.compareTo(other) >= 0),
            LESS_THAN_OR_EQUAL("<=", (self, other) -> self.compareTo(other) <= 0),
            GREATER_THAN(">", (self, other) -> self.compareTo(other) > 0),
            LESS_THAN("<", (self, other) -> self.compareTo(other) < 0),
            EQUAL("==", (self, other) -> self.compareTo(other) == 0),
            ;

            public static final Operator[] VALUES = values();

            private final String symbol;
            private final IVersionOperator operator;

            Operator(String symbol, IVersionOperator operator) {
                this.symbol = symbol;
                this.operator = operator;
            }

            public String symbol() {
                return symbol;
            }

            @Override
            public boolean apply(IVersion self, IVersion other) {
                return this.operator.apply(self, other);
            }
        }

        @FunctionalInterface
        public interface IVersionOperator {
            boolean apply(IVersion self, IVersion other);
        }
    }

    public record ConstraintSet(List<Constraint> constraints) implements IVersionConstraint {
        public static ConstraintSet parse(String constraints) throws InvalidVersionConstraintException {
            String[] clauses = constraints.split(" ");
            List<Constraint> constraintList = new ArrayList<>();
            for (String clause : clauses) {
                Constraint.Operator operator = Constraint.Operator.EQUAL;
                for (Constraint.Operator op : Constraint.Operator.VALUES) {
                    if (clause.startsWith(op.symbol())) {
                        operator = op;
                        clause = clause.substring(op.symbol().length());
                        break;
                    }
                }

                SemVer version;
                try {
                    version = SemVer.parse(clause);
                } catch (InvalidVersionFormatException e) {
                    throw new InvalidVersionConstraintException("Invalid version in constraint", e);
                }
                constraintList.add(new Constraint(version, operator));
            }
            return new ConstraintSet(constraintList);
        }

        @Override
        public @NotNull String toString() {
            return constraints.toString();
        }

        @Override
        public boolean isSatisfiedBy(IVersion version) {
            for (Constraint constraint : constraints) {
                if (!constraint.isSatisfiedBy(version)) {
                    return false;
                }
            }
            return true;
        }
    }
}