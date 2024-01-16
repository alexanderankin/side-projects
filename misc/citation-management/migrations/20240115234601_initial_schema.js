/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  await knex.schema.createTable('citation', t => {
    t.increments();
    t.string('name', 255).notNullable().unique();
    t.text('description').nullable()
    t.timestamps();
  });

  await knex.schema.createTable('citation_citation', t => {
    t.integer('citation_from').notNullable().references('id').inTable('citation');
    t.integer('citation_to').notNullable().references('id').inTable('citation');
    t.timestamps();
    t.unique(['citation_from', 'citation_to']);
  });
}

/**
 * @param { import("knex").Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  await knex.schema.dropTable('citation_citation');
  await knex.schema.dropTable('citation');
}
